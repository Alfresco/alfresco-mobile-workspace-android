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
import java.lang.ref.WeakReference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ActionListViewModel(
    val context: Context,
    state: ActionListState
) : MvRxViewModel<ActionListState>(state) {

    init {
        buildModel()
    }

    private fun buildModel() = withState { state ->
        if (state.entry.isPartial) {
            viewModelScope.launch {
                fetchEntry(state.entry).execute {
                    when (it) {
                        is Success ->
                            ActionListState(it(), makeActions(it()), it)
                        is Fail ->
                            ActionListState(state.entry, makeActions(entry), it)
                        else ->
                            copy(fetch = it)
                    }
                }
            }
        } else {
            setState { copy(actions = makeActions(entry), fetch = Success(entry)) }
        }
    }

    private fun fetchEntry(entry: Entry): Flow<Entry> =
        when (entry.type) {
            Entry.Type.Site -> FavoritesRepository()::getFavoriteSite.asFlow(entry.otherId ?: "")
            else -> BrowseRepository()::fetchNode.asFlow(entry.id)
        }

    fun <T : Action> execute(actionClass: Class<T>) {
        withState { st ->
            st.actions.firstOrNull { actionClass.isInstance(it) }?.execute(context, GlobalScope)
        }
    }

    fun execute(action: Action) {
        val weakSelf = WeakReference(this)
        action.execute(context, GlobalScope) { _action ->
            weakSelf.get()?.setState {
                ActionListState(
                    _action.entry,
                    makeActions(_action.entry)
                )
            }
        }
    }

    private fun makeActions(entry: Entry): List<Action> {
        val actions = mutableListOf<Action>()

        if (entry.isTrashed) {
            actions.add(ActionRestore(entry))
            actions.add(ActionDeleteForever(entry))
            return actions
        }

        actions.add(if (entry.isFavorite) ActionRemoveFavorite(entry) else ActionAddFavorite(entry))
        if (entry.type == Entry.Type.File) actions.add(ActionDownload(entry))
        if (entry.canDelete) actions.add(ActionDelete(entry))
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
