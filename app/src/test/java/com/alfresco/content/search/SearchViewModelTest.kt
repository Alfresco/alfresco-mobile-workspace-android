package com.alfresco.content.search

import com.alfresco.content.data.SearchRepository
import com.alfresco.content.models.SearchItem
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

internal class SearchViewModelTest {

    lateinit var searchViewModel: SearchViewModel

    lateinit var searchRepository: SearchRepository

    @Before
    fun setUp() {

        /*val context = Mockito.mock(Context::class.java)

        Mavericks.initialize(context)*/

        searchRepository = Mockito.mock(SearchRepository::class.java)

//        val session = Session(context.applicationContext, Account("", "", "", "", ""))
        searchViewModel = SearchViewModel(SearchResultsState(), searchRepository)
    }

    @Test
    fun isShowAdvanceFilterView_returnsFalse_ifListEmpty() {

        val result = searchViewModel.isShowAdvanceFilterView(emptyList())

        Assert.assertEquals(result, false)
    }

    @Test
    fun isShowAdvanceFilterView_returnsTrue_ifListNotEmpty() {

        val list = mutableListOf<SearchItem>()
        list.add(SearchItem(name = "Lib", default = true, filterWithContains = true, categories = emptyList(), resetButton = true))

        val result = searchViewModel.isShowAdvanceFilterView(list)

        Assert.assertEquals(result, true)
    }
}
