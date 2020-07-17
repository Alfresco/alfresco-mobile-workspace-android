package com.alfresco.content.browse

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.FavoritesRepository
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.data.SearchRepository
import com.alfresco.content.data.SharedLinksRepository
import com.alfresco.content.data.SitesRepository
import com.alfresco.content.data.TrashCanRepository
import com.alfresco.content.listview.ListViewModel
import kotlin.reflect.KSuspendFunction2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BrowseViewModel(
    state: BrowseViewState,
    val path: String?,
    val context: Context
) : ListViewModel<BrowseViewState>(state) {

    init {
        refresh()

        if (sortOrder() == Entry.SortOrder.ByModifiedDate) {
            BrowseViewState.ModifiedGroup.prepare(context)
        }
    }

    override fun refresh() = fetch()

    override fun fetchNextPage() = fetch(true)

    private fun fetch(nextPage: Boolean = false) = withState { state ->
        val req = fetchRequest()
        val skipCount = if (nextPage) state.entries.count() else 0

        viewModelScope.launch {
            req.invoke(
                skipCount,
                ITEMS_PER_PAGE
            ).execute {
                updateEntries(it(), sortOrder()).copy(request = it)
            }
        }
    }

    private fun fetchRequest(): KSuspendFunction2<Int, Int, Flow<ResponsePaging>> {
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

    private fun sortOrder(): Entry.SortOrder {
        return when (path) {
            context.getString(R.string.nav_path_recents) -> Entry.SortOrder.ByModifiedDate
            else -> Entry.SortOrder.Default
        }
    }

    private suspend fun getNodesInFolder(skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return BrowseRepository().getNodes(path ?: "", skipCount, maxItems)
    }

    companion object : MvRxViewModelFactory<BrowseViewModel, BrowseViewState> {

        override fun create(viewModelContext: ViewModelContext, state: BrowseViewState): BrowseViewModel? {
            return BrowseViewModel(state, viewModelContext.args as? String, viewModelContext.app())
        }
    }
}
