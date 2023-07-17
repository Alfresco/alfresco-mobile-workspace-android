package com.alfresco.content.search

import android.content.Context
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.SearchRepository

data class RecentSearchViewState(
    val entries: List<String> = emptyList(),
) : MavericksState

class RecentSearchViewModel(
    viewState: RecentSearchViewState,
    val context: Context,
) : MavericksViewModel<RecentSearchViewState>(viewState) {

    @Suppress("unused")
    val changeListener = SearchRepository.RecentSearchesChangeListener(context) { refresh() }

    init {
        refresh()
    }

    private fun refresh() {
        setState { copy(entries = SearchRepository().getRecentSearches()) }
    }

    companion object : MavericksViewModelFactory<RecentSearchViewModel, RecentSearchViewState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: RecentSearchViewState,
        ) = RecentSearchViewModel(state, viewModelContext.app())
    }
}
