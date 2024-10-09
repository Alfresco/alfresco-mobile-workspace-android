package com.alfresco.content.viewer.pdf

import com.alfresco.content.viewer.common.PreviewProvider

object PdfPreviewProvider : PreviewProvider {
    override fun isMimeTypeSupported(mimeType: String) = mimeType == "application/pdf"

    override fun createViewer() = PdfViewerFragment()
}
