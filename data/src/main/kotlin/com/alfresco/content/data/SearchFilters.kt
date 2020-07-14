package com.alfresco.content.data

import java.util.EnumSet

enum class SearchFilter {
    Files,
    Folders,
    Libraries;
}

typealias SearchFilters = EnumSet<SearchFilter>

infix fun SearchFilter.and(other: SearchFilter) = SearchFilters.of(this, other)
infix fun SearchFilters.allOf(other: SearchFilters) = this.containsAll(other)
infix fun SearchFilters.and(other: SearchFilter) = SearchFilters.of(other, *this.toTypedArray())
fun emptyFilters(): SearchFilters = SearchFilters.noneOf(SearchFilter::class.java)
