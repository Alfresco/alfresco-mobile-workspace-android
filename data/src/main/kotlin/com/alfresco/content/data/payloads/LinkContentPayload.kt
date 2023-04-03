package com.alfresco.content.data.payloads

import com.alfresco.content.data.Entry

/**
 * Marked as LinkContentPayload
 */
data class LinkContentPayload(
    val source: String = "",
    val mimeType: String = "",
    val sourceId: String = "",
    val name: String = ""
) {
    companion object {
        fun with(entry: Entry): LinkContentPayload {
            return LinkContentPayload(
                source = "alfresco-1-adw-contentAlfresco",
                sourceId = entry.id,
                mimeType = entry.mimeType ?: "",
                name = entry.name
            )
        }
    }
}
