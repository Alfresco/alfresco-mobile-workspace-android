package com.alfresco.content.search

import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.data.SearchRepository
import com.alfresco.content.listview.ListViewModel
import com.alfresco.content.listview.ListViewState
import kotlin.reflect.KSuspendFunction2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class SearchResultsViewModel(
    state: ListViewState,
    val repository: SearchRepository
) : ListViewModel(state) {
    private val inputStream = MutableStateFlow("")
    private val results: MutableStateFlow<ResponsePaging?> = MutableStateFlow(null)
    private var queryString = "" // TODO: State
    private var filters = emptyFilters()

    init {
        viewModelScope.launch {
            inputStream.debounce(300)
                .filterNot { it.isEmpty() || it.length < 3 }
                .collectLatest { query ->
                    val job = launch {
                        results.value = repository.search(
                            query,
                            0,
                            ITEMS_PER_PAGE,
                            filters.contains(SearchFilter.Files),
                            filters.contains(SearchFilter.Folders)
                        )
                    }
                    job.invokeOnCompletion { }
                    job.join()
                }
        }

        viewModelScope.launch {
            results.filterNotNull().collectLatest() {
                val req = Success(it)
                setState {
                    copy(
                        entries = it.entries,
                        req = req
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        queryString = query.trim()
        inputStream.value = queryString
    }

    fun setFilters(filters: SearchFilters) {
        this.filters = filters
        refresh()
    }

    fun clearQuery() = setSearchQuery("")

    override fun fetchRequest(): KSuspendFunction2<Int, Int, Flow<ResponsePaging>> {
        return this::getResults
    }

    private suspend fun getResults(skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return flow {
            emit(
                repository.search(
                    queryString,
                    skipCount,
                    maxItems,
                    filters.contains(SearchFilter.Files),
                    filters.contains(SearchFilter.Folders)
                )
            )
        }
    }

    companion object : MvRxViewModelFactory<SearchResultsViewModel, ListViewState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ListViewState
        ): SearchResultsViewModel? {
            return SearchResultsViewModel(state, SearchRepository())
        }
    }
}
