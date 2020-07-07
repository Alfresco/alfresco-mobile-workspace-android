package com.alfresco.content.browse

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.FavoritesRepository
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.data.SearchRepository
import com.alfresco.content.data.SharedLinksRepository
import com.alfresco.content.data.SitesRepository
import com.alfresco.content.data.TrashCanRepository
import kotlin.reflect.KSuspendFunction2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BrowseViewModel(
    val state: BrowseViewState,
    val path: String?,
    val context: Context
) : MvRxViewModel<BrowseViewState>(state) {

    init {
        fetch()
    }

    fun refresh() = fetch()

    fun fetchNextPage() = fetch(true)

    private fun fetch(nextPage: Boolean = false) = withState { state ->
        val req = fetchRequest(path)
        val skipCount = if (nextPage) state.entries.count() else 0

        viewModelScope.launch {
            req.invoke(skipCount, ITEMS_PER_PAGE).execute {
                val newEntries = it()?.entries ?: emptyList()
                copy(
                    entries = if (nextPage) {
                        entries + newEntries
                    } else {
                        newEntries
                    },
                    req = it
                )
            }
        }
    }

    private fun fetchRequest(path: String?): KSuspendFunction2<Int, Int, Flow<ResponsePaging>> {
        return when (path) {
            context.getString(R.string.nav_path_recents) -> SearchRepository()::getRecents
            context.getString(R.string.nav_path_favorites) -> FavoritesRepository()::getFavorites
            context.getString(R.string.nav_path_my_files) -> BrowseRepository()::getMyFiles
            context.getString(R.string.nav_path_my_libraries) -> SitesRepository()::getMySites
            context.getString(R.string.nav_path_fav_libraries) -> FavoritesRepository()::getFavoriteLibraries
            context.getString(R.string.nav_path_shared) -> SharedLinksRepository()::getSharedLinks
            context.getString(R.string.nav_path_trash) -> TrashCanRepository()::getDeletedNodes
            else -> this::getNodesInFolder
        }
    }

    private suspend fun getNodesInFolder(skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return BrowseRepository().getNodes(path ?: "", skipCount, maxItems)
    }

    companion object : MvRxViewModelFactory<BrowseViewModel, BrowseViewState> {
        private const val ITEMS_PER_PAGE = 25

        override fun create(viewModelContext: ViewModelContext, state: BrowseViewState): BrowseViewModel? {
            return BrowseViewModel(state, viewModelContext.args as? String, viewModelContext.app())
        }
    }
}
