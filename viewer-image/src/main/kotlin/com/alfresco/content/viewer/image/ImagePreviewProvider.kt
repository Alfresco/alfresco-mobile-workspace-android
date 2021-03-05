package com.alfresco.content.viewer.image

import com.alfresco.content.viewer.common.PreviewProvider

object ImagePreviewProvider : PreviewProvider {
    private val supportedImageFormats = setOf(
        "image/bmp",
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp",
        "image/gif",
        "image/svg+xml"
    )

    override fun isMimeTypeSupported(mimeType: String): Boolean =
        supportedImageFormats.contains(mimeType)

    override fun createViewer() = ImageViewerFragment()
}
