package com.alfresco.content.search

import android.content.Context
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.data.SearchRepository

data class RecentSearchViewState(
    val entries: List<String> = emptyList()
) : MvRxState

class RecentSearchViewModel(
    viewState: RecentSearchViewState,
    val context: Context
) : MvRxViewModel<RecentSearchViewState>(viewState) {

    val changeListener = SearchRepository.RecentSearchesChangeListener(context) { refresh() }

    init {
        refresh()
    }

    private fun refresh() {
        setState { copy(entries = SearchRepository().getRecentSearches()) }
    }

    companion object : MvRxViewModelFactory<RecentSearchViewModel, RecentSearchViewState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: RecentSearchViewState
        ) = RecentSearchViewModel(state, viewModelContext.app())
    }
}
