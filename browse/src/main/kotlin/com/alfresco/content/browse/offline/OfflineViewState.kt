package com.alfresco.content.browse.offline

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.listview.ListViewState

data class OfflineViewState(
    val parentId: String? = null,
    override val entries: List<Entry> = emptyList(),
    override val hasMoreItems: Boolean = false,
    override val request: Async<ResponsePaging> = Uninitialized,
    val syncNowEnabled: Boolean = false,
) : ListViewState {

    constructor(args: OfflineBrowseArgs) : this(parentId = args.id)

    override val isCompact: Boolean
        get() = parentId != null

    fun update(response: ResponsePaging?): OfflineViewState {
        if (response == null) return this

        val nextPage = response.pagination.skipCount > 0
        val pageEntries = response.entries
        val newEntries = if (nextPage) { entries + pageEntries } else { pageEntries }

        return copy(entries = newEntries, hasMoreItems = response.pagination.hasMoreItems)
    }

    override fun copy(_entries: List<Entry>): ListViewState = copy(entries = _entries)
}
