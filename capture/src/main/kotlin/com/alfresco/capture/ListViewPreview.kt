package com.alfresco.capture

import android.content.Context
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isVisible
import coil.EventListener
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.load
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.capture.databinding.ViewListPreviewBinding
import java.util.Locale.ENGLISH
import java.util.concurrent.TimeUnit

/**
 * Generated Model View for the Preview Screen
 */
@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class ListViewPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    var captureItem: CaptureItem? = null

    private val binding = ViewListPreviewBinding.inflate(LayoutInflater.from(context), this)
    private val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .eventListener(object : EventListener {
                override fun onSuccess(request: ImageRequest, result: SuccessResult) {
                    super.onSuccess(request, result)
                    onSuccessMediaLoad()
                }
            })
            .build()
    }

    /**
     * Bind the capture item data to the view
     */
    @ModelProp
    fun setData(item: CaptureItem) {
        captureItem = item

        if (item.isVideo()) {
            setVideoScaleType(item)
        } else {
            setPhotoScaleType(item)
        }

        binding.preview.load(item.uri, imageLoader)
    }

    private fun setPhotoScaleType(item: CaptureItem) {
        val exif = item.uri.path?.let { ExifInterface(it) }
        if (exif != null) {
            val rotation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
            binding.preview.scaleType = getScaleType(convertOrientationToDegree(rotation))
        }
    }

    private fun setVideoScaleType(item: CaptureItem) {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(item.uri.path)
        val rotation =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
        rotation?.let {
            binding.preview.scaleType = getScaleType(it.toInt())
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

    /**
     * set clickListener to the preview button to en-large the view
     */
    @CallbackProp
    fun setPreviewClickListener(listener: OnClickListener?) {
        binding.preview.setOnClickListener(listener)
    }

    /**
     * set clickListener to the delete button to delete the view
     */
    @CallbackProp
    fun setDeletePhotoClickListener(listener: OnClickListener?) {
        binding.deletePhotoButton.setOnClickListener(listener)
    }

    companion object {
        const val ORIENTATION_0 = 0
        const val ORIENTATION_90 = 90
        const val ORIENTATION_180 = 180
        const val ORIENTATION_270 = 270
    }

    private fun onSuccessMediaLoad() {
        captureItem?.let {
            if (it.isVideo()) {
                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(it.uri.path)
                val time: String? =
                    mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val duration = time?.toLong()

                binding.videoDuration.isVisible = it.isVideo() == true

                duration?.let { millis ->
                    setVideoDuration(millis)
                }
            }
        }
        binding.deletePhotoButton.isVisible = true
    }

    private fun setVideoDuration(millis: Long) {
        val hour = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes =
            TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(millis),
            )
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(millis),
            )
        val hms = if (hour > 0L) {
            java.lang.String.format(
                ENGLISH,
                context.getString(R.string.format_video_duration_hour),
                hour,
                minutes,
                seconds,
            )
        } else {
            java.lang.String.format(
                ENGLISH,
                context.getString(R.string.format_video_duration_minute),
                minutes,
                seconds,
            )
        }
        binding.videoDuration.text = hms
    }
}
