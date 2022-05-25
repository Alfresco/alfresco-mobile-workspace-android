package com.alfresco.pdf

/**
 * Marked as ImageToPDFOptions class
 */
data class ImageToPDFOptions(
    var mFileName: String?,
    var mPageSize: String?,
    var qualityString: String? = null,
    var mBorderWidth: Int = 0,
    var imagesUri: List<String>,
    var pageColor: Int,
    var imageScaleType: String?,
    var pageNumStyle: String?
) : PDFOptions(
    mFileName, mPageSize, mBorderWidth, pageColor
) {
    var marginTop = 0
        private set
    var marginBottom = 0
        private set
    var marginRight = 0
        private set
    var marginLeft = 0
        private set

    fun setMargins(top: Int, bottom: Int, right: Int, left: Int) {
        marginTop = top
        marginBottom = bottom
        marginRight = right
        marginLeft = left
    }
}
