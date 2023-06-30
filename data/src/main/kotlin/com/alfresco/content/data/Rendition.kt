package com.alfresco.content.data

import java.io.File

data class Rendition(
    val uri: String,
    val mimeType: String,
) {
    val offlineFileName: String
        get() = when (mimeType) {
            PDF_MIME_TYPE -> PDF_FILE_NAME
            IMG_MIME_TYPE -> IMG_FILE_NAME
            else -> throw UnsupportedOperationException()
        }

    companion object {
        private const val PDF_MIME_TYPE = "application/pdf"
        private const val IMG_MIME_TYPE = "image/jpeg"
        private const val PDF_FILE_NAME = ".preview_pdf"
        private const val IMG_FILE_NAME = ".preview_img"

        fun fetchRenditionInPath(path: String): Rendition {
            val pdfPath = "$path/$PDF_FILE_NAME"
            if (File(pdfPath).exists()) {
                return Rendition("file://$pdfPath", PDF_MIME_TYPE)
            }

            val imgPath = "$path/$IMG_FILE_NAME"
            if (File(imgPath).exists()) {
                return Rendition("file://$imgPath", IMG_MIME_TYPE)
            }

            throw UnsupportedOperationException()
        }
    }
}
