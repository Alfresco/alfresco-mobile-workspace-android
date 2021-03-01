package com.alfresco.content.actions

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.FavoritesRepository
import com.alfresco.coroutines.asFlow
import com.alfresco.events.on
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ActionListViewModel(
    val context: Context,
    state: ActionListState
) : MvRxViewModel<ActionListState>(state) {

    init {
        buildModel()

        // Update the model if necessary
        viewModelScope.on<ActionAddFavorite> (block = ::updateState)
        viewModelScope.on<ActionRemoveFavorite> (block = ::updateState)
        viewModelScope.on<ActionAddOffline> (block = ::updateState)
        viewModelScope.on<ActionRemoveOffline> (block = ::updateState)
    }

    private fun buildModel() = withState { state ->
        // If entry is partial and not in the offline tab
        if (state.entry.isPartial && !state.entry.hasOfflineStatus) {
            viewModelScope.launch {
                fetchEntry(state.entry).execute {
                    when (it) {
                        is Success ->
                            ActionListState(it(), makeActions(it()), makeTopActions(it()), it)
                        is Fail ->
                            ActionListState(state.entry, makeActions(entry), makeTopActions(entry), it)
                        else ->
                            copy(fetch = it)
                    }
                }
            }
        } else {
            setState { copy(actions = makeActions(entry), topActions = makeTopActions(entry), fetch = Success(entry)) }
        }
    }

    private fun updateState(action: Action) {
        setState {
            ActionListState(
                action.entry,
                makeActions(action.entry),
                makeTopActions(action.entry)
            )
        }
    }

    private fun fetchEntry(entry: Entry): Flow<Entry> =
        when (entry.type) {
            Entry.Type.SITE -> FavoritesRepository()::getFavoriteSite.asFlow(entry.id)
            else -> BrowseRepository()::fetchEntry.asFlow(entry.id)
        }

    fun <T : Action> execute(actionClass: Class<T>) {
        withState { st ->
            st.actions.firstOrNull { actionClass.isInstance(it) }?.execute(context, GlobalScope)
        }
    }

    fun execute(action: Action) =
        action.execute(context, GlobalScope)

    private fun makeActions(entry: Entry): List<Action> =
        when {
            entry.isTrashed -> {
                actionsForTrashed(entry)
            }
            entry.hasOfflineStatus -> {
                actionsForOffline(entry)
            }
            else -> {
                defaultActionsFor(entry)
            }
        }

    private fun defaultActionsFor(entry: Entry) =
        listOf(
            offlineActionFor(entry),
            favoriteActionFor(entry),
            externalActionsFor(entry),
            deleteActionFor(entry)
        ).flatten()

    private fun actionsForTrashed(entry: Entry): List<Action> =
        listOf(ActionRestore(entry), ActionDeleteForever(entry))

    private fun actionsForOffline(entry: Entry): List<Action> =
        listOf(
            offlineActionFor(entry),
            externalActionsFor(entry)
        ).flatten()

    private fun offlineActionFor(entry: Entry) =
        if (!entry.isFile && !entry.isFolder) {
            listOf()
        } else if (entry.hasOfflineStatus && !entry.isOffline) {
            listOf()
        } else {
            listOf(if (entry.isOffline) ActionRemoveOffline(entry) else ActionAddOffline(entry))
        }

    private fun favoriteActionFor(entry: Entry) =
        listOf(if (entry.isFavorite) ActionRemoveFavorite(entry) else ActionAddFavorite(entry))

    private fun externalActionsFor(entry: Entry) =
        if (entry.type == Entry.Type.FILE) {
            listOf(ActionOpenWith(entry), ActionDownload(entry))
        } else {
            listOf()
        }

    private fun deleteActionFor(entry: Entry) =
        if (entry.canDelete) listOf(ActionDelete(entry)) else listOf()

    private fun makeTopActions(entry: Entry): List<Action> {
        val actions = mutableListOf<Action>()
        if (!entry.hasOfflineStatus) {
            if (entry.isFavorite) {
                actions.add(ActionRemoveFavorite(entry))
            } else {
                actions.add(ActionAddFavorite(entry))
            }
        }
        if (entry.type == Entry.Type.FILE) {
            actions.add(ActionDownload(entry))
        }
        return actions
    }

    companion object : MvRxViewModelFactory<ActionListViewModel, ActionListState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ActionListState
        ): ActionListViewModel? {
            return ActionListViewModel(viewModelContext.activity(), state)
        }
    }
}
