package com.alfresco.content.data

import java.util.EnumSet

enum class SearchFilter {
    Contextual,
    Files,
    Folders,
    Libraries;
}

data class AdvanceSearchFilter(val query: String, val name: String)

typealias SearchFilters = EnumSet<SearchFilter>
typealias AdvanceSearchFilters = MutableList<AdvanceSearchFilter>

infix fun SearchFilter.and(other: SearchFilter): SearchFilters = SearchFilters.of(this, other)
infix fun SearchFilters.allOf(other: SearchFilters) = this.containsAll(other)
infix fun SearchFilters.and(other: SearchFilter): SearchFilters = SearchFilters.of(other, *this.toTypedArray())
fun emptyFilters(): SearchFilters = SearchFilters.noneOf(SearchFilter::class.java)
fun emptyAdvanceFilters(): AdvanceSearchFilters = mutableListOf()
