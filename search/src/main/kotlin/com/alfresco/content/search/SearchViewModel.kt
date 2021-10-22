package com.alfresco.content.search

import android.content.Context
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.AdvanceSearchFilter
import com.alfresco.content.data.AdvanceSearchFilters
import com.alfresco.content.data.SearchFilter
import com.alfresco.content.data.SearchFilters
import com.alfresco.content.data.SearchRepository
import com.alfresco.content.data.emptyAdvanceFilters
import com.alfresco.content.listview.ListViewModel
import com.alfresco.content.listview.ListViewState
import com.alfresco.content.models.AppConfigModel
import com.alfresco.content.models.SearchItem
import java.util.concurrent.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

data class SearchParams(
    val terms: String,
    val contextId: String?,
    val filters: SearchFilters,
    val advanceSearchFilter: AdvanceSearchFilters,
    val skipCount: Int,
    val maxItems: Int = ListViewModel.ITEMS_PER_PAGE
)

class SearchViewModel(
    val context: Context,
    state: SearchResultsState,
    private val repository: SearchRepository
) : ListViewModel<SearchResultsState>(state) {
    private val liveSearchEvents: MutableStateFlow<SearchParams>
    private val searchEvents: MutableStateFlow<SearchParams>
    private val appConfigModel: AppConfigModel
    private var params: SearchParams

    init {
        setState { copy(filters = defaultFilters(state)) }

        // TODO: move search params to state object
        params = SearchParams("", state.contextId, defaultFilters(state), defaultAdvanceFilters(state), 0)
        liveSearchEvents = MutableStateFlow(params)
        searchEvents = MutableStateFlow(params)
        appConfigModel = repository.getAppConfig()

        setState { copy(listSearchFilters = appConfigModel.search) }

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

    /**
     * returns the all available search filters
     */
    fun getSearchFilterList(): List<SearchItem>? {
        return appConfigModel.search
    }

    /**
     * It updates the selectedFilter index after tap on filter any filter dropdown item
     */
    fun copyFilterIndex(position: Int) {
        setState {
            copy(selectedFilterIndex = position)
        }
        updateSearchChipCategoryList(position)
    }

    /**
     * returns the default selected name from the search filter list using index.
     */
    fun getDefaultSearchFilterName(state: SearchResultsState): String? {
        val defaultFilter = state.listSearchFilters?.get(state.selectedFilterIndex)
        if (defaultFilter != null)
            return defaultFilter.name
        return null
    }

    /**
     * returns the index of search filter item, or -1 if the list doesn't contain the search filter item.
     */
    fun getDefaultSearchFilterIndex(list: List<SearchItem>?): Int {
        return list?.indexOf(list.find { it.default == true }) ?: -1
    }

    /**
     * updated the search chip for relative filter by selecting it from dropdown
     */
    fun updateSearchChipCategoryList(index: Int) {
        val list = mutableListOf<SearchChipCategory>()

        getSearchFilterList()?.get(index)?.categories?.forEach { categoryItem ->
            list.add(SearchChipCategory(categoryItem, false))
        }

        setState { copy(listSearchCategoryChips = list) }
    }

    /**
     * returns filter data on the selection on item in filter menu
     */
    fun getSelectedFilter(index: Int, state: SearchResultsState): SearchItem? {
        return state.listSearchFilters?.get(index)
    }

    /**
     * true if search filters available otherwise false
     */
    fun isShowAdvanceFilterView(list: List<SearchItem>?): Boolean {
        return !list.isNullOrEmpty()
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

    private fun defaultAdvanceFilters(state: SearchResultsState): AdvanceSearchFilters {
        val list = emptyAdvanceFilters()
        if (state.isContextual)
            AdvanceSearchFilter(SearchFilter.Contextual.name, SearchFilter.Contextual.name)

        list.add(AdvanceSearchFilter(SearchFilter.Files.name, SearchFilter.Files.name))
        list.add(AdvanceSearchFilter(SearchFilter.Folders.name, SearchFilter.Folders.name))
        return list
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

    /**
     * set advance filters to search params
     */
    fun setFilters(advanceSearchFilter: AdvanceSearchFilters) {
        // Avoid triggering refresh when filters don't change
        if (advanceSearchFilter != params.advanceSearchFilter) {
            params = params.copy(advanceSearchFilter = advanceSearchFilter)
            refresh()
        }
    }

    /**
     * update chip name after apply or reset the component
     */
    fun updateChipName(state: SearchResultsState, model: SearchChipCategory, name: String) {
        val list = mutableListOf<SearchChipCategory>()

        state.listSearchCategoryChips?.forEachIndexed { index, obj ->
            if (obj == model) {
                list.add(
                    SearchChipCategory(
                        obj.category,
                        isSelected = name.isNotEmpty(), selectedName = name, selectedQuery = obj.selectedQuery
                    )
                )
            } else
                list.add(obj)
        }

        setState { copy(listSearchCategoryChips = list) }
    }

    /**
     * update isSelected when chip is selected
     */
    fun updateSelected(state: SearchResultsState, model: SearchChipCategory, isSelected: Boolean) {
        val list = mutableListOf<SearchChipCategory>()

        state.listSearchCategoryChips?.forEachIndexed { index, obj ->
            if (obj == model) {
                list.add(
                    SearchChipCategory(
                        obj.category,
                        isSelected = isSelected, selectedName = obj.selectedName, selectedQuery = obj.selectedQuery
                    )
                )
            } else
                list.add(obj)
        }

        setState { copy(listSearchCategoryChips = list) }
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
        ) = SearchViewModel(viewModelContext.activity(), state, SearchRepository())
    }
}
