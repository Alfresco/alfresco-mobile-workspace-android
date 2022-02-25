package com.alfresco.content.data

data class ResponsePaging(
    val entries: List<Entry>,
    val pagination: Pagination,
    val facetContext: FacetContext? = null
) {
    companion object {
        fun with(raw: com.alfresco.content.models.NodeChildAssociationPaging): ResponsePaging {
            return ResponsePaging(
                raw.list?.entries?.map { Entry.with(it.entry) } ?: emptyList(),
                Pagination.with(raw.list!!.pagination!!)
            )
        }

        /**
         * return the response with updated extension value
         */
        fun withExtension(raw: com.alfresco.content.models.NodeChildAssociationPaging): ResponsePaging {
            return ResponsePaging(
                raw.list?.entries?.map { Entry.with(it.entry, true) } ?: emptyList(),
                Pagination.with(raw.list!!.pagination!!)
            )
        }

        fun with(raw: com.alfresco.content.models.ResultSetPaging): ResponsePaging {
            return ResponsePaging(
                raw.list?.entries?.map { Entry.with(it.entry) } ?: emptyList(),
                Pagination.with(raw.list!!.pagination!!),
                FacetContext.with(raw.list?.context)
            )
        }

        /**
         * returns Response for search while sharing the files through extension
         */
        fun withExtension(raw: com.alfresco.content.models.ResultSetPaging): ResponsePaging {
            return ResponsePaging(
                raw.list?.entries?.map { Entry.with(it.entry, true) } ?: emptyList(),
                Pagination.with(raw.list!!.pagination!!),
                FacetContext.with(raw.list?.context)
            )
        }

        fun with(raw: com.alfresco.content.models.SitePaging): ResponsePaging {
            return ResponsePaging(
                raw.list.entries.map { Entry.with(it.entry) },
                Pagination.with(raw.list.pagination)
            )
        }

        fun with(raw: com.alfresco.content.models.FavoritePaging): ResponsePaging {
            return ResponsePaging(
                raw.list.entries.map { Entry.with(it.entry) },
                Pagination.with(raw.list.pagination)
            )
        }

        fun with(raw: com.alfresco.content.models.SiteRolePaging): ResponsePaging {
            return ResponsePaging(
                raw.list.entries.map { Entry.with(it.entry) },
                Pagination.with(raw.list.pagination)
            )
        }

        fun with(raw: com.alfresco.content.models.SharedLinkPaging): ResponsePaging {
            return ResponsePaging(
                raw.list.entries.map { Entry.with(it.entry) },
                Pagination.with(raw.list.pagination)
            )
        }

        fun with(raw: com.alfresco.content.models.DeletedNodesPaging): ResponsePaging {
            return ResponsePaging(
                raw.list?.entries?.map { Entry.with(it.entry!!) } ?: emptyList(),
                Pagination.with(raw.list!!.pagination!!)
            )
        }
    }
}
