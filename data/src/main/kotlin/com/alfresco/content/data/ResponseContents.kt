package com.alfresco.content.data

import com.alfresco.process.models.ResultContents

/**
 * Marked as ResponseContents class
 */
data class ResponseContents(
    val size: Int,
    val total: Int,
    val start: Int,
    val listContents: List<Entry>,
) {
    companion object {
        /**
         * return the ResponseComments obj using ResultComments
         */
        fun with(raw: ResultContents): ResponseContents {
            return ResponseContents(
                size = raw.size ?: 0,
                total = raw.total ?: 0,
                start = raw.start ?: 0,
                listContents = raw.data?.map { Entry.with(it, uploadServer = UploadServerType.NONE) } ?: emptyList(),
            )
        }
    }
}
