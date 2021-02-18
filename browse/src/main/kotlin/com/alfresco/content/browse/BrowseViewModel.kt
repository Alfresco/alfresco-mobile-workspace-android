package com.alfresco.content.browse

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.actions.ActionAddFavorite
import com.alfresco.content.actions.ActionDeleteForever
import com.alfresco.content.actions.ActionRemoveFavorite
import com.alfresco.content.actions.ActionRestore
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.FavoritesRepository
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.data.SearchRepository
import com.alfresco.content.data.SharedLinksRepository
import com.alfresco.content.data.SitesRepository
import com.alfresco.content.data.TrashCanRepository
import com.alfresco.content.listview.ListViewModel
import com.alfresco.content.listview.ListViewState
import com.alfresco.coroutines.asFlow
import com.alfresco.events.on
import java.lang.IllegalStateException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BrowseViewModel(
    state: BrowseViewState,
    val context: Context
) : ListViewModel<BrowseViewState>(state) {

    init {
        refresh()

        withState {
            if (it.sortOrder == BrowseViewState.SortOrder.ByModifiedDate) {
                BrowseViewState.ModifiedGroup.prepare(context)
            }
        }

        if (state.path == context.getString(R.string.nav_path_favorites)) {
            val types = setOf(Entry.Type.File, Entry.Type.Folder)
            viewModelScope.on<ActionAddFavorite> { it.entry.ifType(types, ::refresh) }
            viewModelScope.on<ActionRemoveFavorite> { it.entry.ifType(types, ::removeEntry) }
        }

        if (state.path == context.getString(R.string.nav_path_fav_libraries)) {
            val types = setOf(Entry.Type.Site)
            viewModelScope.on<ActionAddFavorite> { it.entry.ifType(types, ::refresh) }
            viewModelScope.on<ActionRemoveFavorite> { it.entry.ifType(types, ::removeEntry) }
        }

        if (state.path == context.getString(R.string.nav_path_trash)) {
            viewModelScope.on<ActionRestore> { removeEntry(it.entry) }
            viewModelScope.on<ActionDeleteForever> { removeEntry(it.entry) }
        }
    }

    @Suppress("ControlFlowWithEmptyBody")
    private fun Entry.ifType(
        types: Set<Entry.Type>,
        block: (entry: Entry) -> Unit
    ) = if (types.contains(type)) { block(this) } else { }

    @Suppress("UNUSED_PARAMETER")
    private fun refresh(ignored: Entry) = refresh()

    override fun refresh() = fetch()

    override fun fetchNextPage() = fetch(true)

    private fun fetch(nextPage: Boolean = false) = withState { state ->
        val path = state.path
        val nodeId = state.nodeId
        val skipCount = if (nextPage) state.baseEntries.count() else 0

        viewModelScope.launch {
            loadResults(
                path,
                nodeId,
                skipCount,
                ITEMS_PER_PAGE
            ).execute {
                if (it is Loading) {
                    copy(request = it)
                } else {
                    update(it()).copy(request = it)
                }
            }
        }
    }

    private suspend fun loadResults(path: String, item: String?, skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return when (path) {
            context.getString(R.string.nav_path_recents) ->
                SearchRepository()::getRecents.asFlow(skipCount, maxItems)

            context.getString(R.string.nav_path_favorites) ->
                FavoritesRepository()::getFavorites.asFlow(skipCount, maxItems)

            context.getString(R.string.nav_path_my_libraries) ->
                SitesRepository()::getMySites.asFlow(skipCount, maxItems)

            context.getString(R.string.nav_path_fav_libraries) ->
                FavoritesRepository()::getFavoriteLibraries.asFlow(skipCount, maxItems)

            context.getString(R.string.nav_path_shared) ->
                SharedLinksRepository()::getSharedLinks.asFlow(skipCount, maxItems)

            context.getString(R.string.nav_path_trash) ->
                TrashCanRepository()::getDeletedNodes.asFlow(skipCount, maxItems)

            context.getString(R.string.nav_path_folder) ->
                BrowseRepository()::fetchFolderItems.asFlow(requireNotNull(item), skipCount, maxItems)

            context.getString(R.string.nav_path_site) ->
                BrowseRepository()::fetchLibraryItems.asFlow(requireNotNull(item), skipCount, maxItems)

            else -> throw IllegalStateException()
        }
    }

    override fun emptyMessageArgs(state: ListViewState) =
        when ((state as BrowseViewState).path) {
            context.getString(R.string.nav_path_recents) ->
                Triple(R.drawable.ic_empty_recent, R.string.recent_empty_title, R.string.recent_empty_message)
            context.getString(R.string.nav_path_favorites) ->
                Triple(R.drawable.ic_empty_favorites, R.string.favorite_files_empty_title, R.string.favorites_empty_message)
            context.getString(R.string.nav_path_fav_libraries) ->
                Triple(R.drawable.ic_empty_favorites, R.string.favorite_sites_empty_title, R.string.favorites_empty_message)
            else ->
                Triple(R.drawable.ic_empty_folder, R.string.folder_empty_title, R.string.folder_empty_message)
        }

    companion object : MvRxViewModelFactory<BrowseViewModel, BrowseViewState> {

        override fun create(viewModelContext: ViewModelContext, state: BrowseViewState): BrowseViewModel? {
            return BrowseViewModel(state, viewModelContext.app())
        }
    }
}
