package com.alfresco.content.browse

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.SearchRepository
import kotlinx.coroutines.launch

class BrowseViewModel(
    state: BrowseViewState,
    path: String?,
    context: Context
) : MvRxViewModel<BrowseViewState>(state) {

    init {
        val req = when (path) {
            "recents" -> SearchRepository(context)::getRecents
            else -> BrowseRepository(context)::getNodes
        }
        viewModelScope.launch {
            req.invoke().execute { copy(nodes = it) }
        }
    }

    companion object : MvRxViewModelFactory<BrowseViewModel, BrowseViewState> {
        override fun create(viewModelContext: ViewModelContext, state: BrowseViewState): BrowseViewModel? {
            return BrowseViewModel(state, viewModelContext.args as? String, viewModelContext.app())
        }
    }
}
