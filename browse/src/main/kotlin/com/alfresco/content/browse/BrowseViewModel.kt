package com.alfresco.content.browse

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.FavoritesRepository
import com.alfresco.content.data.SearchRepository
import com.alfresco.content.data.SharedLinksRepository
import com.alfresco.content.data.SitesRepository
import com.alfresco.content.data.TrashCanRepository
import kotlinx.coroutines.launch

class BrowseViewModel(
    val state: BrowseViewState,
    val path: String?,
    val context: Context
) : MvRxViewModel<BrowseViewState>(state) {

    init {
        fetchNextPage()
    }

    fun fetchNextPage() = withState { state ->
        val req = when (path) {
            context.getString(R.string.nav_path_recents) -> SearchRepository()::getRecents
            context.getString(R.string.nav_path_favorites) -> FavoritesRepository()::getFavorites
            context.getString(R.string.nav_path_my_files) -> BrowseRepository()::getMyFiles
            context.getString(R.string.nav_path_my_libraries) -> SitesRepository()::getMySites
            context.getString(R.string.nav_path_fav_libraries) -> FavoritesRepository()::getFavoriteLibraries
            context.getString(R.string.nav_path_shared) -> SharedLinksRepository()::getSharedLinks
            context.getString(R.string.nav_path_trash) -> TrashCanRepository()::getDeletedNodes
            else -> null
        }

        viewModelScope.launch {
            if (req != null) {
                req.invoke(state.entries.count(), ITEMS_PER_PAGE).execute {
                    copy(
                        entries = entries + (it()?.entries ?: emptyList()),
                        req = it
                    )
                }
            } else {
                BrowseRepository().getNodes(path ?: "", state.entries.count(), ITEMS_PER_PAGE).execute {
                    copy(
                        entries = entries + (it()?.entries ?: emptyList()),
                        req = it
                    )
                }
            }
        }
    }

    companion object : MvRxViewModelFactory<BrowseViewModel, BrowseViewState> {
        private const val ITEMS_PER_PAGE = 25

        override fun create(viewModelContext: ViewModelContext, state: BrowseViewState): BrowseViewModel? {
            return BrowseViewModel(state, viewModelContext.args as? String, viewModelContext.app())
        }
    }
}
