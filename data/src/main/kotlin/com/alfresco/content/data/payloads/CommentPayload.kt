package com.alfresco.content.data.payloads

data class CommentPayload(
    val message: String = ""
) {
    companion object {
        fun with(message: String): CommentPayload {
            return CommentPayload(message = message)
        }
    }
}
