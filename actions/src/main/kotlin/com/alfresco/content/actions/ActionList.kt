package com.alfresco.content.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.data.Entry
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.lang.ref.WeakReference
import kotlinx.android.synthetic.main.fragment_list_item_actions.recycler_view
import kotlinx.coroutines.GlobalScope

data class ActionListState(
    val actions: List<Action> = emptyList(),
    val entry: Entry
) : MvRxState {
    constructor(target: Entry) : this(entry = target)
}

class ActionListViewModel(
    state: ActionListState
) : MvRxViewModel<ActionListState>(state) {

    init {
        val actions = mutableListOf<Action>()
        actions.add(if (state.entry.isFavorite) Action.RemoveFavorite(state.entry) else Action.AddFavorite(state.entry))

        if (BuildConfig.DEBUG) {
            actions.add(Action.Download(state.entry))
        }

        if (state.entry.canDelete) actions.add(Action.Delete(state.entry))
        setState { copy(actions = actions) }
    }
}

class ActionListFragment(parent: ActionListSheet) : BaseMvRxFragment(R.layout.fragment_list_item_actions) {
    private val viewModel: ActionListViewModel by fragmentViewModel()
    private val parent = WeakReference(parent)

    override fun invalidate() = withState(viewModel) { state ->
        recycler_view.withModels {
            state.actions.forEach {
                actionListRow {
                    id(it.title)
                    action(it)
                    clickListener { _ ->
                        it.execute(GlobalScope) // TODO: use dedicated scope
                        parent.get()?.dismiss()
                    }
                }
            }
        }
    }
}

class ActionListSheet(private val entry: Entry) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.sheet_list_item_actions, container, false)

        if (savedInstanceState == null) {
            val contentFragment = ActionListFragment(this)
            contentFragment.arguments = bundleOf(MvRx.KEY_ARG to entry)
            childFragmentManager
                .beginTransaction()
                .replace(R.id.content, contentFragment)
                .commit()
        }

        return view
    }
}
