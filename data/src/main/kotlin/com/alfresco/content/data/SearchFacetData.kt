package com.alfresco.content.data

import com.alfresco.content.models.FieldsItem
import com.alfresco.content.models.IntervalsItem
import com.alfresco.content.models.QueriesItem
import com.alfresco.content.models.SetsItem

data class SearchFacetData(
    val searchFacetFields: SearchFacetFields, val searchFacetQueries: SearchFacetQueries,
    val searchFacetIntervals: SearchFacetIntervals
)

typealias SearchFacetFields = MutableList<FieldsItem>
typealias SearchFacetQueries = MutableList<QueriesItem>
typealias SearchFacetIntervals = MutableList<IntervalsItem>
typealias SearchFacetSets = MutableList<SetsItem>




fun emptySearchFacetFields(): SearchFacetFields = mutableListOf()
fun emptySearchFacetQueries(): SearchFacetQueries = mutableListOf()
fun emptySearchFacetIntervals(): SearchFacetIntervals = mutableListOf()
fun emptySearchFacetSets(): SearchFacetSets = mutableListOf()