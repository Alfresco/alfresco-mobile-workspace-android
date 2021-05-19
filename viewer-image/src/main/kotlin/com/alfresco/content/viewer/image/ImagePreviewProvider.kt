package com.alfresco.content.viewer.image

import android.os.Build
import com.alfresco.content.viewer.common.PreviewProvider

object ImagePreviewProvider : PreviewProvider {
    private val supportedImageFormats = mutableSetOf(
        "image/bmp",
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp",
        "image/gif",
        "image/svg+xml"
    ).apply {
        if (Build.VERSION.SDK_INT >= 26) {
            add("image/heic")
        }
    }

    override fun isMimeTypeSupported(mimeType: String): Boolean =
        supportedImageFormats.contains(mimeType)

    override fun createViewer() = ImageViewerFragment()
}
