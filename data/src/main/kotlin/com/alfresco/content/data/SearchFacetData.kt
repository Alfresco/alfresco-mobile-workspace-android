package com.alfresco.content.data

import com.alfresco.content.models.FieldsItem
import com.alfresco.content.models.IntervalsItem
import com.alfresco.content.models.QueriesItem
import com.alfresco.content.models.SetsItem

/**
 * Mark as SearchFacetData class
 */
data class SearchFacetData(
    val searchFacetFields: SearchFacetFields,
    val searchFacetQueries: SearchFacetQueries,
    val searchFacetIntervals: SearchFacetIntervals
)

typealias SearchFacetFields = MutableList<FieldsItem>
typealias SearchFacetQueries = MutableList<QueriesItem>
typealias SearchFacetIntervals = MutableList<IntervalsItem>
typealias SearchFacetSets = MutableList<SetsItem>

/**
 * returns the empty list of FieldsItem type
 */
fun emptySearchFacetFields(): SearchFacetFields = mutableListOf()
/**
 * returns the empty list of QueriesItem type
 */
fun emptySearchFacetQueries(): SearchFacetQueries = mutableListOf()
/**
 * returns the empty list of IntervalsItem type
 */
fun emptySearchFacetIntervals(): SearchFacetIntervals = mutableListOf()
/**
 * returns the empty list of SetsItem type
 */
fun emptySearchFacetSets(): SearchFacetSets = mutableListOf()
