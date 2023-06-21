package com.alfresco.content.viewer.image

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.target.ImageViewTarget
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.viewer.common.ChildViewerFragment
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView

class ImageViewerFragment : ChildViewerFragment(R.layout.viewer_image), MavericksView {

    private val viewModel: ImageViewerViewModel by fragmentViewModel()

    private lateinit var largeScaleViewer: SubsamplingScaleImageView
    private lateinit var compatViewer: PhotoView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container = view.findViewById<FrameLayout>(R.id.container)

        withState(viewModel) { state ->
            if (state.largeScale) {
                largeScaleViewer = setupLargeScalePreview(container)
            } else {
                compatViewer = setupCompatPreview(container)
            }
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        if (state.largeScale) {
            if (state.path is Success) {
                largeScaleViewer.loadImage(state.path() ?: "")
            }
        } else {
            compatViewer.loadImage(state.uri)
        }
    }

    private fun setupLargeScalePreview(container: FrameLayout): SubsamplingScaleImageView {
        val view = SubsamplingScaleImageView(container.context)
        view.orientation = SubsamplingScaleImageView.ORIENTATION_USE_EXIF

        view.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        container.addView(view)
        return view
    }

    private fun SubsamplingScaleImageView.loadImage(path: String) {
        loadingListener?.onContentLoaded()
        setImage(ImageSource.uri(path))
    }

    private fun setupCompatPreview(container: FrameLayout): PhotoView {
        val view = PhotoView(container.context)

        // Override double-tap behavior to bypass medium zoom
        view.setOnDoubleTapListener(object : GestureDetector.OnDoubleTapListener {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                return false
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (e == null) return false

                if (view.scale > view.minimumScale) {
                    view.setScale(view.minimumScale, e.x, e.y, true)
                } else {
                    view.setScale(view.maximumScale, e.x, e.y, true)
                }

                return true
            }

            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                return false
            }
        })

        view.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        container.addView(view)
        return view
    }

    private fun PhotoView.loadImage(uri: String) {
        val imageLoader = ImageLoader.Builder(requireContext())
            .componentRegistry {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder(context))
                } else {
                    add(GifDecoder())
                }
                add(SvgDecoder(requireContext()))
            }
            .build()

        val request = ImageRequest.Builder(context)
            .data(uri)
            .target(object : ImageViewTarget(this) {
                override fun onSuccess(result: Drawable) {
                    super.onSuccess(result)
                    loadingListener?.onContentLoaded()
                }

                override fun onError(error: Drawable?) {
                    super.onError(error)
                    loadingListener?.onContentError()
                }
            })
            .build()
        imageLoader.enqueue(request)
    }
}
