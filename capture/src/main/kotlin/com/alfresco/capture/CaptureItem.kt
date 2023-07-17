package com.alfresco.capture

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Parcelize
data class CaptureItem(
    val uri: Uri,
    val mimeType: String,
    val name: String,
    val description: String = "",
) : Parcelable {
    val filename: String
        get() = "$name$extension"
    val extension: String
        get() = when (mimeType) {
            PHOTO_MIMETYPE -> PHOTO_EXTENSION
            VIDEO_MIMETYPE -> VIDEO_EXTENSION
            else -> throw IllegalArgumentException()
        }

    internal fun isPhoto() =
        mimeType == PHOTO_MIMETYPE

    internal fun isVideo() =
        mimeType == VIDEO_MIMETYPE

    internal companion object {
        const val PHOTO_EXTENSION = ".jpg"
        const val VIDEO_EXTENSION = ".mp4"
        const val PHOTO_MIMETYPE = "image/jpeg"
        const val VIDEO_MIMETYPE = "video/mp4"
        private const val PHOTO_NAME_PREFIX = "IMG_"
        private const val VIDEO_NAME_PREFIX = "VID_"

        fun photoCapture(uri: Uri) =
            CaptureItem(uri, PHOTO_MIMETYPE, defaultFilename(PHOTO_NAME_PREFIX))

        fun videoCapture(uri: Uri) =
            CaptureItem(uri, VIDEO_MIMETYPE, defaultFilename(VIDEO_NAME_PREFIX))

        private fun defaultFilename(prefix: String): String {
            val formatter = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US)
            val time: Date = Calendar.getInstance().time
            return "$prefix${formatter.format(time)}"
        }
    }
}
