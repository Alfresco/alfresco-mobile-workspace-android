package com.alfresco.content.data

import com.alfresco.process.models.ResultComments

/**
 * Marked as ResponseComments class
 */
data class ResponseComments(
    val size: Int,
    val total: Int,
    val start: Int,
    val listComments: List<CommentEntry>,
) {
    companion object {
        /**
         * return the ResponseComments obj using ResultComments
         */
        fun with(raw: ResultComments): ResponseComments {
            return ResponseComments(
                size = raw.size ?: 0,
                total = raw.total ?: 0,
                start = raw.start ?: 0,
                listComments = raw.data?.map { CommentEntry.with(it) } ?: emptyList(),
            )
        }
    }
}
