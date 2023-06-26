package com.alfresco.content.data

import java.util.EnumSet

enum class SearchFilter {
    Contextual,
    Files,
    Folders,
    Libraries,
}

/**
 * This class pointing the selected filter chips data.
 * @property query
 * @property name
 */
data class AdvanceSearchFilter(val query: String, val name: String)

typealias SearchFilters = EnumSet<SearchFilter>

/**
 * creating the alias for mutable list of type AdvanceSearchFilter
 */
typealias AdvanceSearchFilters = MutableList<AdvanceSearchFilter>

infix fun SearchFilter.and(other: SearchFilter): SearchFilters = SearchFilters.of(this, other)
infix fun SearchFilters.allOf(other: SearchFilters) = this.containsAll(other)
infix fun SearchFilters.and(other: SearchFilter): SearchFilters = SearchFilters.of(other, *this.toTypedArray())
fun emptyFilters(): SearchFilters = SearchFilters.noneOf(SearchFilter::class.java)

/**
 * empty the mutable list of type AdvanceSearchFilter
 */
fun emptyAdvanceFilters(): AdvanceSearchFilters = mutableListOf()
