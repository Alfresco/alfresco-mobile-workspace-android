package com.alfresco.content.viewer.image

import android.graphics.drawable.Drawable
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.target.ImageViewTarget
import com.alfresco.content.viewer.common.ContentDownloader
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView
import java.io.File
import kotlinx.coroutines.launch

class ImageViewerFragment(
    private val documentId: String,
    private val uri: String,
    private val mimeType: String
) : Fragment(R.layout.viewer_image) {

    private lateinit var progressIndicator: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressIndicator = view.findViewById<ProgressBar>(R.id.progress_indicator)
        val container = view.findViewById<FrameLayout>(R.id.container)

        if (mimeType == "image/jpeg" ||
            mimeType == "image/png") {
            setupLargeScalePreview(container).loadImage(uri)
        } else {
            setupCompatPreview(container).loadImage(uri)
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

    private fun SubsamplingScaleImageView.loadImage(uri: String) {
        val output = File(requireContext().cacheDir, "content.tmp")

        lifecycleScope.launch {
            ContentDownloader.downloadFileTo(uri, output.path)

            progressIndicator.visibility = View.GONE
            setImage(ImageSource.uri(output.path))
        }
    }

    private fun setupCompatPreview(container: FrameLayout): PhotoView {
        val view = PhotoView(container.context)

        // Override double-tap behavior to bypass medium zoom
        view.setOnDoubleTapListener(object : GestureDetector.OnDoubleTapListener {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                return false
            }

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                if (e == null) return false

                if (view.scale > view.minimumScale) {
                    view.setScale(view.minimumScale, e.x, e.y, true)
                } else {
                    view.setScale(view.maximumScale, e.x, e.y, true)
                }

                return true
            }

            override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
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
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder())
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
                    progressIndicator.visibility = View.GONE
                }
            })
            .build()
        imageLoader.enqueue(request)
    }
}
