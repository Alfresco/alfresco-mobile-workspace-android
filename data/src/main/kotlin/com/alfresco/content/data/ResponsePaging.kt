package com.alfresco.content.data

data class ResponsePaging(
    val entries: List<Entry>,
    val pagination: Pagination
) {
    companion object {
        fun with(raw: com.alfresco.content.models.NodeChildAssociationPaging): ResponsePaging {
            return ResponsePaging(
                raw.list?.entries?.map { Entry.with(it.entry) } ?: emptyList(),
                Pagination.with(raw.list!!.pagination!!)
            )
        }

        fun with(raw: com.alfresco.content.models.ResultSetPaging): ResponsePaging {
            return ResponsePaging(
                raw.list?.entries?.map { Entry.with(it.entry) } ?: emptyList(),
                Pagination.with(raw.list!!.pagination!!)
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
