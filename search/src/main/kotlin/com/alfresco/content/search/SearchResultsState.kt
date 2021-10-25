package com.alfresco.content.search

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.data.SearchFilters
import com.alfresco.content.data.emptyFilters
import com.alfresco.content.listview.ListViewState
import com.alfresco.content.models.SearchItem

/**
 * ResultState for Search Controller
 */
data class SearchResultsState(
    override val entries: List<Entry> = emptyList(),
    override val hasMoreItems: Boolean = false,
    override val request: Async<ResponsePaging> = Uninitialized,

    val selectedFilterIndex: Int = -1,
    val listSearchFilters: List<SearchItem>? = emptyList(),
    val listSearchCategoryChips: List<SearchChipCategory>? = emptyList(),
    val filters: SearchFilters = emptyFilters(),
    val contextId: String? = null,
    val contextTitle: String? = null
) : ListViewState {

    constructor(args: ContextualSearchArgs) : this(contextId = args.id, contextTitle = args.title)

    val isContextual: Boolean
        get() {
            return contextId != null
        }

    override val isCompact: Boolean
        get() {
            return entries.firstOrNull()?.type == Entry.Type.SITE
        }

    /**
     * update entries as per response from the server
     */
    fun updateEntries(response: ResponsePaging?): SearchResultsState {
        if (response == null) return this

        val nextPage = response.pagination.skipCount > 0
        val pageEntries = response.entries
        val newEntries = if (nextPage) {
            entries + pageEntries
        } else {
            pageEntries
        }

        return copy(entries = newEntries, hasMoreItems = response.pagination.hasMoreItems)
    }

    override fun copy(_entries: List<Entry>): ListViewState = copy(entries = _entries)
}
