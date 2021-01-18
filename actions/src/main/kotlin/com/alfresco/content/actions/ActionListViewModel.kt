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
        if (state.entry.isPartial) {
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
            Entry.Type.Site -> FavoritesRepository()::getFavoriteSite.asFlow(entry.id)
            else -> BrowseRepository()::fetchEntry.asFlow(entry.id)
        }

    fun <T : Action> execute(actionClass: Class<T>) {
        withState { st ->
            st.actions.firstOrNull { actionClass.isInstance(it) }?.execute(context, GlobalScope)
        }
    }

    fun execute(action: Action) =
        action.execute(context, GlobalScope)

    private fun makeActions(entry: Entry): List<Action> {
        val actions = mutableListOf<Action>()

        if (entry.isTrashed) {
            actions.add(ActionRestore(entry))
            actions.add(ActionDeleteForever(entry))
            return actions
        }

        if (entry.type == Entry.Type.File) {
            if (entry.isOffline) {
                actions.add(ActionRemoveOffline(entry))
            } else {
                actions.add(ActionAddOffline(entry))
            }
        }

        actions.add(if (entry.isFavorite) ActionRemoveFavorite(entry) else ActionAddFavorite(entry))
        if (entry.type == Entry.Type.File) {
            actions.add(ActionOpenWith(entry))
            actions.add(ActionDownload(entry))
        }
        if (entry.canDelete) actions.add(ActionDelete(entry))
        return actions
    }

    private fun makeTopActions(entry: Entry): List<Action> {
        val actions = mutableListOf<Action>()
        actions.add(if (entry.isFavorite) ActionRemoveFavorite(entry) else ActionAddFavorite(entry))
        if (entry.type == Entry.Type.File) {
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
