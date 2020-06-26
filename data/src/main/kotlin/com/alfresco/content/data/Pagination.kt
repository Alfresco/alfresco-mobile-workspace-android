package com.alfresco.content.data

data class Pagination(
    val count: Long,
    val hasMoreItems: Boolean,
    val skipCount: Long,
    val maxItems: Long,
    val totalItems: Long? = null
) {
    companion object {
        fun with(raw: com.alfresco.content.models.Pagination): Pagination {
            return Pagination(
                raw.count,
                raw.hasMoreItems,
                raw.skipCount,
                raw.maxItems,
                raw.totalItems
            )
        }
    }
}
