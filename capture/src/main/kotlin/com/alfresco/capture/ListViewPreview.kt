package com.alfresco.capture

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import coil.ImageLoader
import coil.fetch.VideoFrameFileFetcher
import coil.load
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

    private val binding = ViewListPreviewBinding.inflate(LayoutInflater.from(context), this)
    private val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(context)
            .componentRegistry {
                add(VideoFrameFileFetcher(context))
            }
            .build()
    }

    @ModelProp
    fun setData(item: CaptureItem) {
        binding.playIcon.isVisible = item.isVideo() == true
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
