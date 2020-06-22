package com.alfresco.content.search

import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.data.SearchRepository
import com.alfresco.content.models.ResultNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class SearchViewModel(
    state: SearchViewState,
    repository: SearchRepository
) : MvRxViewModel<SearchViewState>(state) {
    private val query = ConflatedBroadcastChannel<String>()
    private val results = ConflatedBroadcastChannel<List<ResultNode>>()

    init {
        viewModelScope.launch {
            query.asFlow()
                .debounce(300)
                .collectLatest { query ->
                    val job = async(Dispatchers.IO) {
                        results.send(repository.search(query))
                    }
                    job.invokeOnCompletion { }
                    job.await()
                }
        }

        viewModelScope.launch { results.asFlow().execute { copy(results = it) } }
    }

    fun setSearchQuery(query: String) {
        this.query.sendBlocking(query)
    }

    fun clearQuery() = setSearchQuery("")

    companion object : MvRxViewModelFactory<SearchViewModel, SearchViewState> {
        override fun create(viewModelContext: ViewModelContext, state: SearchViewState): SearchViewModel? {
            return SearchViewModel(state, SearchRepository())
        }
    }
}
