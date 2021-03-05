package com.alfresco.content.data

interface PreviewRegistry {
    fun isPreviewSupported(mimeType: String?): Boolean
}
