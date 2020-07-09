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
            // MNT-20822: hasMoreItems returns true incorrectly in some cases
            val hasMoreItems =
                raw.totalItems?.let { raw.count + raw.skipCount < it } ?: raw.hasMoreItems
            return Pagination(
                raw.count,
                hasMoreItems,
                raw.skipCount,
                raw.maxItems,
                raw.totalItems
            )
        }
    }
}
