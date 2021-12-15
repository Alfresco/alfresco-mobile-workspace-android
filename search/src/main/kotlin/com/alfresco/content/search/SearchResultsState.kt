package com.alfresco.content.search

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.Buckets
import com.alfresco.content.data.Entry
import com.alfresco.content.data.FacetFields
import com.alfresco.content.data.FacetIntervals
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

        if (newEntries.isNotEmpty()) {
            val list: MutableList<SearchChipCategory>? = listSearchCategoryChips?.toMutableList()

            val facetFields = response.facetContext?.facetResponse?.facetFields
            val facetIntervals = response.facetContext?.facetResponse?.facetIntervals

            var isFacetFilterSelected = false
            run outForEach@{
                list?.forEach { filterChip ->
                    if (filterChip.fieldsItem != null && filterChip.isSelected) {
                        isFacetFilterSelected = true
                        return@outForEach
                    }
                    if (filterChip.intervalsItem != null && filterChip.isSelected) {
                        isFacetFilterSelected = true
                        return@outForEach
                    }
                }
            }

            facetFields?.forEach { fieldObj ->
                val obj = list?.find { data -> data.fieldsItem?.label == fieldObj.label }
                if (obj == null)
                    list?.add(SearchChipCategory.withDefaultFacet(fieldObj))
                else {
                    if (isFacetFilterSelected)
                        fieldObj.buckets = getFieldBucketList(obj, fieldObj)
                    list[list.indexOf(obj)] = SearchChipCategory.updateFacet(obj, fieldObj)
                }
            }

            facetIntervals?.forEach { intervalObj ->
                val obj = list?.find { data -> data.intervalsItem?.label == intervalObj.label }
                if (obj == null)
                    list?.add(SearchChipCategory.withDefaultFacet(intervalObj))
                else {
                    if (isFacetFilterSelected)
                        intervalObj.buckets = getIntervalBucketList(obj, intervalObj)
                    list[list.indexOf(obj)] = SearchChipCategory.updateFacet(obj, intervalObj)
                }
            }

            if (list != listSearchCategoryChips)
                return copy(entries = newEntries, hasMoreItems = response.pagination.hasMoreItems, listSearchCategoryChips = list)
        }

        return copy(entries = newEntries, hasMoreItems = response.pagination.hasMoreItems)
    }

    private fun getFieldBucketList(obj: SearchChipCategory, fieldObj: FacetFields): List<Buckets> {
        val listBuckets: MutableList<Buckets> = mutableListOf()
        obj.fieldsItem?.buckets?.forEach { oldBucket ->
            val bucketObj = Buckets.updateFieldBucketCount(oldBucket)
            run outForEachBucket@{
                fieldObj.buckets?.forEach { newBucket ->
                    if (oldBucket.filterQuery == newBucket.filterQuery) {
                        bucketObj.count = newBucket.count
                        return@outForEachBucket
                    }
                }
            }
            listBuckets.add(bucketObj)
        }
        return listBuckets
    }

    private fun getIntervalBucketList(obj: SearchChipCategory, intervalsItem: FacetIntervals): List<Buckets> {
        val listBuckets: MutableList<Buckets> = mutableListOf()
        obj.intervalsItem?.buckets?.forEach { oldBucket ->
            val bucketObj = Buckets.updateIntervalBucketCount(oldBucket)
            run outForEachBucket@{
                intervalsItem.buckets?.forEach { newBucket ->
                    if (oldBucket.filterQuery == newBucket.filterQuery) {
                        bucketObj.metrics?.get(0)?.value?.count = newBucket.metrics?.get(0)?.value?.count
                        return@outForEachBucket
                    }
                }
            }
            listBuckets.add(bucketObj)
        }
        return listBuckets
    }

    override fun copy(_entries: List<Entry>): ListViewState = copy(entries = _entries)
}
