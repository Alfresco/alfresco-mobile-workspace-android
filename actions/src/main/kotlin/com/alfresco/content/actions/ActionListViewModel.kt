package com.alfresco.content.actions

import com.alfresco.content.MvRxViewModel
import com.alfresco.content.data.Entry
import java.lang.ref.WeakReference
import kotlinx.coroutines.GlobalScope

class ActionListViewModel(
    state: ActionListState
) : MvRxViewModel<ActionListState>(state) {

    init {
        setState { copy(actions = makeActions(entry)) }
    }

    fun <T : Action> execute(actionClass: Class<T>) {
        withState { st ->
            st.actions.firstOrNull { actionClass.isInstance(it) }?.execute(GlobalScope)
        }
    }

    fun execute(action: Action) {
        val weakSelf = WeakReference(this)
        action.execute(GlobalScope) { _action ->
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
        actions.add(if (entry.isFavorite) Action.RemoveFavorite(entry) else Action.AddFavorite(entry))

        if (BuildConfig.DEBUG) {
            actions.add(Action.Download(entry))
        }

        if (entry.canDelete) actions.add(Action.Delete(entry))
        return actions
    }
}
