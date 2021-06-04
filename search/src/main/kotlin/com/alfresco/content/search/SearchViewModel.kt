package com.alfresco.content.search

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.data.SearchFilter
import com.alfresco.content.data.SearchFilters
import com.alfresco.content.data.SearchRepository
import com.alfresco.content.data.emptyFilters
import com.alfresco.content.listview.ListViewModel
import com.alfresco.content.listview.ListViewState
import java.util.concurrent.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

data class SearchResultsState(
    override val entries: List<Entry> = emptyList(),
    override val hasMoreItems: Boolean = false,
    override val request: Async<ResponsePaging> = Uninitialized,

    val filters: SearchFilters = emptyFilters(),
    val contextId: String? = null,
    val contextTitle: String? = null
) : ListViewState {

    constructor(args: ContextualSearchArgs) : this(contextId = args.id, contextTitle = args.title)

    val isContextual: Boolean
        get() { return contextId != null }

    override val isCompact: Boolean
        get() { return entries.firstOrNull()?.type == Entry.Type.SITE }

    fun updateEntries(response: ResponsePaging?): SearchResultsState {
        if (response == null) return this

        val nextPage = response.pagination.skipCount > 0
        val pageEntries = response.entries
        val newEntries = if (nextPage) { entries + pageEntries } else { pageEntries }

        return copy(entries = newEntries, hasMoreItems = response.pagination.hasMoreItems)
    }

    override fun copy(_entries: List<Entry>): ListViewState = copy(entries = _entries)
}

data class SearchParams(
    val terms: String,
    val contextId: String?,
    val filters: SearchFilters,
    val skipCount: Int,
    val maxItems: Int = ListViewModel.ITEMS_PER_PAGE
)

class SearchViewModel(
    state: SearchResultsState,
    private val repository: SearchRepository
) : ListViewModel<SearchResultsState>(state) {
    private val liveSearchEvents: MutableStateFlow<SearchParams>
    private val searchEvents: MutableStateFlow<SearchParams>
    private var params: SearchParams

    init {
        setState { copy(filters = defaultFilters(state)) }

        // TODO: move search params to state object
        params = SearchParams("", state.contextId, defaultFilters(state), 0)
        liveSearchEvents = MutableStateFlow(params)
        searchEvents = MutableStateFlow(params)

        viewModelScope.launch {
            merge(
                liveSearchEvents.debounce(DEFAULT_DEBOUNCE_TIME),
                searchEvents
            ).filter {
                it.terms.length >= MIN_QUERY_LENGTH
            }.executeOnLatest({ repository.search(it.terms, it.contextId, it.filters, it.skipCount, it.maxItems) }) {
                if (it is Loading) {
                    copy(request = it)
                } else {
                    updateEntries(it()).copy(request = it)
                }
            }
        }
    }

    private suspend fun <T, V> Flow<T>.executeOnLatest(
        action: suspend (value: T) -> V,
        stateReducer: SearchResultsState.(Async<V>) -> SearchResultsState
    ) {
        collectLatest {
            setState { stateReducer(Loading()) }
            try {
                val result = action(it)
                setState { stateReducer(Success(result)) }
            } catch (e: CancellationException) {
                // No-op
            } catch (e: Throwable) {
                setState { stateReducer(Fail(e)) }
            }
        }
    }

    private fun defaultFilters(state: SearchResultsState): SearchFilters {
        return if (state.isContextual) {
            SearchFilters.of(
                SearchFilter.Contextual,
                SearchFilter.Files,
                SearchFilter.Folders
            )
        } else {
            SearchFilters.of(
                SearchFilter.Files,
                SearchFilter.Folders
            )
        }
    }

    fun setSearchQuery(query: String) {
        params = params.copy(terms = query, skipCount = 0)
        liveSearchEvents.value = params
    }

    fun getSearchQuery(): String {
        return params.terms
    }

    fun setFilters(filters: SearchFilters) {
        // Avoid triggering refresh when filters don't change
        if (filters != params.filters) {
            params = params.copy(filters = filters)
            refresh()
        }
    }

    fun saveSearch() {
        repository.saveSearch(params.terms)
    }

    override fun refresh() {
        params = params.copy(skipCount = 0)
        searchEvents.value = params
    }

    override fun fetchNextPage() {
        withState {
            params = params.copy(skipCount = it.entries.count())
            searchEvents.value = params
        }
    }

    override fun emptyMessageArgs(state: ListViewState) =
        Triple(R.drawable.ic_empty_search, R.string.search_empty_title, R.string.search_empty_message)

    companion object : MavericksViewModelFactory<SearchViewModel, SearchResultsState> {
        const val MIN_QUERY_LENGTH = 3
        const val DEFAULT_DEBOUNCE_TIME = 300L

        override fun create(
            viewModelContext: ViewModelContext,
            state: SearchResultsState
        ) = SearchViewModel(state, SearchRepository())
    }
}
