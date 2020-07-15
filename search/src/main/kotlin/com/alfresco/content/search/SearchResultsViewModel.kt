package com.alfresco.content.search

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.data.SearchFilters
import com.alfresco.content.data.SearchRepository
import com.alfresco.content.data.emptyFilters
import com.alfresco.content.getStringList
import com.alfresco.content.listview.ListViewModel
import com.alfresco.content.listview.ListViewState
import com.alfresco.content.putStringList
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

data class SearchResultsState(
    override val entries: List<Entry> = emptyList(),
    override val request: Async<ResponsePaging> = Uninitialized
) : ListViewState

data class SearchParams(
    val terms: String,
    val filters: SearchFilters,
    val skipCount: Int,
    val maxItems: Int = ListViewModel.ITEMS_PER_PAGE
)

class SearchResultsViewModel(
    state: SearchResultsState,
    private val repository: SearchRepository
) : ListViewModel<SearchResultsState>(state) {
    private val liveSearchEvents = ConflatedBroadcastChannel<SearchParams>()
    private val searchEvents = ConflatedBroadcastChannel<SearchParams>()
    private var params = SearchParams("", emptyFilters(), 0)

    init {
        viewModelScope.launch {
            merge(
                liveSearchEvents.asFlow().debounce(DEFAULT_DEBOUNCE_TIME),
                searchEvents.asFlow()
            ).filter {
                it.terms.isNotEmpty()
            }.executeOnLatest({ repository.search(it.terms, it.filters, it.skipCount, it.maxItems) }) {
                if (it is Loading) {
                    copy(request = it)
                } else {
                    val newEntries = it()?.entries ?: emptyList()
                    val skipCount = it()?.pagination?.skipCount ?: 0L
                    copy(
                        entries = if (skipCount != 0L) {
                            entries + newEntries
                        } else {
                            newEntries
                        },
                        request = it
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        params = params.copy(terms = query, skipCount = 0)
        liveSearchEvents.sendBlocking(params)
    }

    fun setFilters(filters: SearchFilters) {
        // Avoid triggering refresh when filters don't change
        if (filters != params.filters) {
            params = params.copy(filters = filters)
            refresh()
        }
    }

    fun saveSearch(context: Context) {
        val key = context.getString(R.string.recent_searches_key)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        var list = sharedPrefs.getStringList(key).toMutableList()

        // At most 15 distinct values, with the latest added to top
        list.remove(params.terms)
        list.add(0, params.terms)
        list = list.subList(0, minOf(list.count(), 15))

        val editor = sharedPrefs.edit()
        editor.putStringList(key, list)
        editor.apply()
    }

    fun clearQuery() = setSearchQuery("")

    override fun refresh() {
        params = params.copy(skipCount = 0)
        searchEvents.sendBlocking(params)
    }

    override fun fetchNextPage() {
        withState {
            params = params.copy(skipCount = it.entries.count())
            searchEvents.sendBlocking(params)
        }
    }

    companion object : MvRxViewModelFactory<SearchResultsViewModel, SearchResultsState> {
        const val DEFAULT_DEBOUNCE_TIME = 300L

        override fun create(
            viewModelContext: ViewModelContext,
            state: SearchResultsState
        ): SearchResultsViewModel? {
            return SearchResultsViewModel(state, SearchRepository())
        }
    }
}
