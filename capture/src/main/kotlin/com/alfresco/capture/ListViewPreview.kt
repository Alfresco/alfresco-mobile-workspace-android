package com.alfresco.capture

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import coil.EventListener
import coil.ImageLoader
import coil.fetch.VideoFrameFileFetcher
import coil.load
import coil.request.ImageRequest
import coil.request.ImageResult
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.capture.databinding.ViewListPreviewBinding

@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class ListViewPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var captureItem: CaptureItem? = null

    private val binding = ViewListPreviewBinding.inflate(LayoutInflater.from(context), this)
    private val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(context)
            .componentRegistry {
                add(VideoFrameFileFetcher(context))
            }
            .eventListener(object : EventListener {
                override fun onSuccess(request: ImageRequest, metadata: ImageResult.Metadata) {
                    super.onSuccess(request, metadata)
                        captureItem?.let {
                            binding.playIcon.isVisible = it.isVideo() == true
                        }
                    binding.deletePhotoButton.isVisible = true
                }
            })
            .build()
    }

    @ModelProp
    fun setData(item: CaptureItem) {
        captureItem = item
        binding.preview.load(item.uri, imageLoader)
    }

    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        setOnClickListener(listener)
    }

    @CallbackProp
    fun setPreviewClickListener(listener: OnClickListener?) {
        binding.preview.setOnClickListener(listener)
    }

    @CallbackProp
    fun setDeletePhotoClickListener(listener: OnClickListener?) {
        binding.deletePhotoButton.setOnClickListener(listener)
    }
}
