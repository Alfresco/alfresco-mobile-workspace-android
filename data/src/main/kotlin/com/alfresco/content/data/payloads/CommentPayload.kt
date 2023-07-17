package com.alfresco.content.data.payloads

/**
 * Marked as CommentPayload class
 */
data class CommentPayload(
    val message: String = "",
) {
    companion object {
        /**
         * returns the CommentPayload obj
         */
        fun with(message: String): CommentPayload {
            return CommentPayload(message = message)
        }
    }
}
