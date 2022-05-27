package com.alfresco.scan

import android.net.Uri
import android.os.Parcelable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.parcelize.Parcelize

/**
 * Marked as ScanItem class
 */
@Parcelize
data class ScanItem(
    val uri: Uri,
    val mimeType: String,
    val name: String,
    val description: String = ""
) : Parcelable {
    val filename: String
        get() = "$name$extension"
    val extension: String
        get() = when (mimeType) {
            PHOTO_MIMETYPE -> PHOTO_EXTENSION
            PDF_MIMETYPE -> PDF_EXTENSION
            else -> throw IllegalArgumentException()
        }

    internal fun isPhoto() =
        mimeType == PHOTO_MIMETYPE

    internal fun isPdf() =
        mimeType == PDF_MIMETYPE

    internal companion object {
        const val PHOTO_EXTENSION = ".jpg"
        const val PDF_EXTENSION = ".pdf"
        const val PHOTO_MIMETYPE = "image/jpeg"
        const val PDF_MIMETYPE = "application/pdf"
        private const val PHOTO_NAME_PREFIX = "IMG_"
        private const val PDF_NAME_PREFIX = "SCAN_"

        fun photoCapture(uri: Uri) =
            ScanItem(uri, PHOTO_MIMETYPE, defaultFilename(PHOTO_NAME_PREFIX))

        fun pdfCapture(uri: Uri, name: String) =
            ScanItem(uri, PDF_MIMETYPE, name)

        private fun defaultFilename(prefix: String): String {
            val formatter = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US)
            val time: Date = Calendar.getInstance().time
            return "$prefix${formatter.format(time)}"
        }

        fun defaultPdfFilename(): String {
            val formatter = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US)
            val time: Date = Calendar.getInstance().time
            return "$PDF_NAME_PREFIX${formatter.format(time)}"
        }
    }
}
