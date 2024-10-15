package com.alfresco.content.browse.offline

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.listview.ListViewState

data class OfflineViewState(
    val parentId: String? = null,
    override val entries: List<Entry> = emptyList(),
    override val selectedEntries: List<Entry> = emptyList(),
    override val hasMoreItems: Boolean = false,
    override val request: Async<ResponsePaging> = Uninitialized,
    override val maxLimitReachedForMultiSelection: Boolean = false,
    val syncNowEnabled: Boolean = false,
) : ListViewState {
    constructor(args: OfflineBrowseArgs) : this(parentId = args.id)

    override val isCompact: Boolean
        get() = parentId != null

    fun update(response: ResponsePaging?): OfflineViewState {
        if (response == null) return this

        val nextPage = response.pagination.skipCount > 0

        val selectedEntriesMap = selectedEntries.associateBy { it.id }

        val pageEntries =
            response.entries.map { entry ->
                val isSelectedForMultiSelection = selectedEntriesMap[entry.id]?.isSelectedForMultiSelection ?: false
                entry.copy(isSelectedForMultiSelection = isSelectedForMultiSelection)
            }.toMutableList()

        val newEntries =
            if (nextPage) {
                entries + pageEntries
            } else {
                pageEntries
            }

        return copy(entries = newEntries, hasMoreItems = response.pagination.hasMoreItems)
    }

    override fun copy(_entries: List<Entry>): ListViewState = copy(entries = _entries)
}
