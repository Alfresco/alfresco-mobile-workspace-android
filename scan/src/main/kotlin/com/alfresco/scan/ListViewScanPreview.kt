package com.alfresco.scan

import android.content.Context
import android.media.ExifInterface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import coil.EventListener
import coil.ImageLoader
import coil.fetch.VideoFrameFileFetcher
import coil.load
import coil.request.ImageRequest
import coil.request.ImageResult
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.scan.databinding.ViewScanListPreviewBinding

/**
 * Generated Model View for the Preview Screen
 */
@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListViewScanPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var scanItem: ScanItem? = null

    private val binding = ViewScanListPreviewBinding.inflate(LayoutInflater.from(context), this)
    private val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(context)
            .componentRegistry {
                add(VideoFrameFileFetcher(context))
            }
            .eventListener(object : EventListener {
                override fun onSuccess(request: ImageRequest, metadata: ImageResult.Metadata) {
                    super.onSuccess(request, metadata)
                }
            })
            .build()
    }

    /**
     * Bind the capture item data to the view
     */
    @ModelProp
    fun setData(item: ScanItem) {
        scanItem = item

        setPhotoScaleType(item)

        binding.preview.load(item.uri, imageLoader)
    }

    private fun setPhotoScaleType(item: ScanItem) {
        val exif = item.uri.path?.let { ExifInterface(it) }
        if (exif != null) {
            val rotation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            binding.preview.scaleType = getScaleType(convertOrientationToDegree(rotation))
        }
    }

    private fun convertOrientationToDegree(orientation: Int): Int {
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> ORIENTATION_90
            ExifInterface.ORIENTATION_ROTATE_270 -> ORIENTATION_270
            ExifInterface.ORIENTATION_ROTATE_180 -> ORIENTATION_180
            else -> ORIENTATION_0
        }
    }

    private fun getScaleType(rotation: Int): ImageView.ScaleType {
        val isTablet = context.resources.getBoolean(R.bool.isTablet)
        return when {
            isTablet && (rotation == ORIENTATION_0 || rotation == ORIENTATION_180) -> {
                ImageView.ScaleType.FIT_CENTER
            }
            !isTablet && (rotation == ORIENTATION_180 || rotation == ORIENTATION_0) -> {
                ImageView.ScaleType.FIT_CENTER
            }
            else -> {
                ImageView.ScaleType.FIT_XY
            }
        }
    }

    /**
     * set clickListener to the list item
     */
    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        setOnClickListener(listener)
    }

    companion object {
        const val ORIENTATION_0 = 0
        const val ORIENTATION_90 = 90
        const val ORIENTATION_180 = 180
        const val ORIENTATION_270 = 270
    }
}
