package com.alfresco.content.viewer.common

interface PreviewProvider {

    fun isMimeTypeSupported(mimeType: String): Boolean

    fun createViewer(): ChildViewerFragment
}
