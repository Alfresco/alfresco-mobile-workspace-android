package com.alfresco.content.viewer

import com.alfresco.content.data.PreviewRegistry
import com.alfresco.content.viewer.common.PreviewProvider
import com.alfresco.content.viewer.image.ImagePreviewProvider
import com.alfresco.content.viewer.media.MediaPreviewProvider
import com.alfresco.content.viewer.pdf.PdfPreviewProvider
import com.alfresco.content.viewer.text.TextPreviewProvider

object ViewerRegistry : PreviewRegistry {
    private val providers = mutableListOf<PreviewProvider>()

    fun setup() {
        register(PdfPreviewProvider)
        register(ImagePreviewProvider)
        register(MediaPreviewProvider)
        register(TextPreviewProvider)
    }

    private fun register(provider: PreviewProvider) {
        providers.add(provider)
    }

    override fun isPreviewSupported(mimeType: String?): Boolean {
        if (mimeType == null) return false
        return providers
            .map { it.isMimeTypeSupported(mimeType) }
            .reduce { acc, next -> acc || next }
    }

    fun previewProvider(mimeType: String?): PreviewProvider? {
        if (mimeType == null) return null
        return providers.firstOrNull { it.isMimeTypeSupported(mimeType) }
    }
}
