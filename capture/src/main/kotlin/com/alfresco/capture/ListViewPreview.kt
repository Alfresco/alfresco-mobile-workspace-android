package com.alfresco.capture

import android.content.Context
import android.media.ExifInterface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
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

        binding.preview.scaleType = getScaleType(item)

        binding.preview.load(item.uri, imageLoader)
    }

    private fun getScaleType(item: CaptureItem): ImageView.ScaleType {
        val isTablet = context.resources.getBoolean(R.bool.isTablet)
        val exif = item.uri.path?.let { ExifInterface(it) }
        if (exif != null) {
            val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            return when {
                isTablet && (rotation == ExifInterface.ORIENTATION_ROTATE_180 || rotation == ExifInterface.ORIENTATION_NORMAL) -> {
                    ImageView.ScaleType.FIT_XY
                }
                !isTablet && (rotation == ExifInterface.ORIENTATION_ROTATE_90 || rotation == ExifInterface.ORIENTATION_ROTATE_270) -> {
                    ImageView.ScaleType.FIT_XY
                }
                else -> {
                    ImageView.ScaleType.FIT_CENTER
                }
            }
        }
        return ImageView.ScaleType.FIT_XY
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
