package com.alfresco.content.browse

import android.content.Context
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.listview.ListViewModel
import com.alfresco.content.listview.ListViewState
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.FavoritesRepository
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.data.SearchRepository
import com.alfresco.content.data.SharedLinksRepository
import com.alfresco.content.data.SitesRepository
import com.alfresco.content.data.TrashCanRepository
import kotlin.reflect.KSuspendFunction2
import kotlinx.coroutines.flow.Flow

class BrowseViewModel(
    state: ListViewState,
    val path: String?,
    val context: Context
) : ListViewModel(state) {

    init {
        refresh()
    }

    override fun fetchRequest(): KSuspendFunction2<Int, Int, Flow<ResponsePaging>> {
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

    companion object : MvRxViewModelFactory<BrowseViewModel, ListViewState> {

        override fun create(viewModelContext: ViewModelContext, state: ListViewState): BrowseViewModel? {
            return BrowseViewModel(state, viewModelContext.args as? String, viewModelContext.app())
        }
    }
}
