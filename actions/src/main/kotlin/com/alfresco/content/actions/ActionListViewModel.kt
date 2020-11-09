package com.alfresco.content.actions

import android.content.Context
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.data.Entry
import java.lang.ref.WeakReference
import kotlinx.coroutines.GlobalScope

class ActionListViewModel(
    val context: Context,
    state: ActionListState
) : MvRxViewModel<ActionListState>(state) {

    init {
        setState { copy(actions = makeActions(entry)) }
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

        if (BuildConfig.DEBUG) {
            if (entry.type == Entry.Type.File) actions.add(ActionDownload(entry))
        }

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
