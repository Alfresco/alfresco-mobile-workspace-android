package com.alfresco.content.search

import com.alfresco.content.data.SearchRepository
import org.junit.Assert
import org.junit.Test

internal class SearchViewModelTest {

    @Test
    fun isShowAdvanceFilterView_returnsFalse_ifListEmpty() {

        val searchViewModel = SearchViewModel(SearchResultsState(), SearchRepository())

//        val list = mutableListOf<SearchItem>()
//        list.add(SearchItem(name = "Lib", default = true, filterWithContains = true, categories = emptyList(), resetButton = true))

        val result = searchViewModel.isShowAdvanceFilterView(emptyList())

        Assert.assertEquals(result, false)
    }
}
