package com.alfresco.content.search

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.Buckets
import com.alfresco.content.data.Entry
import com.alfresco.content.data.Facets
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.data.SearchFacetFields
import com.alfresco.content.data.SearchFacetIntervals
import com.alfresco.content.data.SearchFacetQueries
import com.alfresco.content.data.SearchFilters
import com.alfresco.content.data.emptyFilters
import com.alfresco.content.data.emptySearchFacetFields
import com.alfresco.content.data.emptySearchFacetIntervals
import com.alfresco.content.data.emptySearchFacetQueries
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
    val listFacetQueries: SearchFacetQueries = emptySearchFacetQueries(),
    val listFacetIntervals: SearchFacetIntervals = emptySearchFacetIntervals(),
    val listFacetFields: SearchFacetFields = emptySearchFacetFields(),
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

        val facets = response.facetContext?.facetResponse?.facets
        val list: MutableList<SearchChipCategory>? = listSearchCategoryChips?.toMutableList()
        val isFacetFilterSelected = isChipSelected(list)

        // reset all facet chip bucket count to zero
        list?.forEach { chip ->
            chip.facets?.let { facetObj ->
                list[list.indexOf(chip)] = SearchChipCategory.updateFacet(chip, Facets.updateFacetBucketWithZeroCount(facetObj))
            }
        }

        when (isFacetFilterSelected) {

            true -> {
                if (newEntries.isNotEmpty()) {
                    // updating the new facet's data
                    facets?.forEach { newFacetObj ->
                        // returns the SearchChipCategory obj that matches facets label otherwise null
                        val obj = list?.find { data -> data.facets?.label == newFacetObj.label }

                        if (obj == null)
                            list?.add(SearchChipCategory.withDefaultFacet(newFacetObj))
                        else {
                            if (isFacetFilterSelected)
                                newFacetObj.buckets = getFacetBucketList(obj, newFacetObj)
                            list[list.indexOf(obj)] = SearchChipCategory.updateFacet(obj, newFacetObj)
                        }
                    }
                }
                if (list != listSearchCategoryChips)
                    return copy(entries = newEntries, hasMoreItems = response.pagination.hasMoreItems, listSearchCategoryChips = list)
            }
            false -> {
                if (newEntries.isNotEmpty()) {
                    val updateList = list?.filter { it.category?.component?.selector != ChipComponentType.FACETS.component }?.toMutableList()
                    facets?.forEach { facetObj ->
                        updateList?.add(SearchChipCategory.withFilterCountZero(facetObj))
                    }
                    if (updateList != listSearchCategoryChips)
                        return copy(entries = newEntries, hasMoreItems = response.pagination.hasMoreItems, listSearchCategoryChips = updateList)

                }

            }
        }
        return copy(entries = newEntries, hasMoreItems = response.pagination.hasMoreItems)
    }

    private fun isChipSelected(list: MutableList<SearchChipCategory>?): Boolean {
        val facetChipObj = list?.find { filterChip -> filterChip.category?.component != null && filterChip.isSelected }
        return facetChipObj != null
    }

    private fun getFacetBucketList(obj: SearchChipCategory, facets: Facets): List<Buckets> {
        val listBuckets: MutableList<Buckets> = mutableListOf()
        obj.facets?.buckets?.forEach { oldBucket ->
            val bucketObj = Buckets.updateIntervalBucketCount(oldBucket)
            val newBucketObj = facets.buckets?.find { newBucket -> oldBucket.filterQuery == newBucket.filterQuery }
            if (newBucketObj != null)
                bucketObj.metrics?.get(0)?.value?.count = newBucketObj.metrics?.get(0)?.value?.count
            listBuckets.add(bucketObj)
        }
        return listBuckets
    }

    override fun copy(_entries: List<Entry>): ListViewState = copy(entries = _entries)
}
