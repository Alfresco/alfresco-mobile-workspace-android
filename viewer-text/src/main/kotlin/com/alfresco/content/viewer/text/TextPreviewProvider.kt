package com.alfresco.content.viewer.text

import com.alfresco.content.viewer.common.PreviewProvider

object TextPreviewProvider : PreviewProvider {
    override fun isMimeTypeSupported(mimeType: String) = mimeType.startsWith("text/")

    override fun createViewer() = TextViewerFragment()
}
