package com.alfresco.content.browse

import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.data.BrowseRepository
import kotlinx.coroutines.launch

class BrowseViewModel(
    state: BrowseViewState,
    repository: BrowseRepository
) : MvRxViewModel<BrowseViewState>(state) {

    init {
        viewModelScope.launch {
            repository.getNodes().execute { copy(nodes = it) }
        }
    }

    companion object : MvRxViewModelFactory<BrowseViewModel, BrowseViewState> {
        override fun create(viewModelContext: ViewModelContext, state: BrowseViewState): BrowseViewModel? {
            return BrowseViewModel(state, BrowseRepository(viewModelContext.app()))
        }
    }
}
