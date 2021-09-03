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
import coil.fetch.VideoFrameFileFetcher
import coil.load
import coil.request.ImageRequest
import coil.request.ImageResult
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.capture.databinding.ViewListPreviewBinding
import java.util.Locale.ENGLISH
import java.util.concurrent.TimeUnit

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
                        onSuccessMediaLoad()
                    }
                    binding.deletePhotoButton.isVisible = true
                }
            })
            .build()
    }

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
            val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            binding.preview.scaleType = getScaleType(convertOrientationToDegree(rotation), true)
        }
    }

    private fun setVideoScaleType(item: CaptureItem) {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(item.uri.path)
        val rotation = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
        rotation?.let {
            binding.preview.scaleType = getScaleType(it.toInt(), false)
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

    private fun getScaleType(rotation: Int, isPhoto: Boolean): ImageView.ScaleType {
        val isTablet = context.resources.getBoolean(R.bool.isTablet)
        return when {
            isTablet && (rotation == ORIENTATION_0 || rotation == ORIENTATION_180) -> {
                ImageView.ScaleType.FIT_CENTER
            }
            isPhoto && isTablet && (rotation == ORIENTATION_90 || rotation == ORIENTATION_270) -> {
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

    companion object {
        const val ORIENTATION_0 = 0
        const val ORIENTATION_90 = 0
        const val ORIENTATION_180 = 0
        const val ORIENTATION_270 = 0
    }

    private fun onSuccessMediaLoad() {
        captureItem?.let {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(it.uri.path)
            val time: String? = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = time?.toLong()

            duration?.let { millis ->
                val hms = java.lang.String.format(
                    ENGLISH,
                    context.getString(R.string.format_video_duration), TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
                )

                binding.videoDuration.isVisible = it.isVideo() == true
                binding.videoDuration.text = hms
            }
        }
        binding.deletePhotoButton.isVisible = true
    }
}
