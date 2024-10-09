package com.alfresco.content.viewer.media

import com.alfresco.content.viewer.common.PreviewProvider

object MediaPreviewProvider : PreviewProvider {
    override fun isMimeTypeSupported(mimeType: String) = mimeType.startsWith("audio/") || mimeType.startsWith("video/")

    override fun createViewer() = MediaViewerFragment()
}
