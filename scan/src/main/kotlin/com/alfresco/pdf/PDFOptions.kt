package com.alfresco.pdf

/**
 * Marked as PDFOptions class
 */
open class PDFOptions internal constructor(
    mFileName: String?,
    mPageSize: String?,
    mBorderWidth: Int,
    pageColor: Int
) {
    var outFileName: String? = mFileName
    var pageSize: String? = mPageSize
    var borderWidth = mBorderWidth
}
