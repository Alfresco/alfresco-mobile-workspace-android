package com.alfresco.content.actions.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.BaseMvRxBottomSheet
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.actions.Action
import com.alfresco.content.actions.actionListRow
import com.alfresco.content.actions.databinding.SheetActionCreateBinding
import com.alfresco.content.data.Entry
import kotlinx.coroutines.GlobalScope

data class ActionCreateState(
    val parent: Entry,
    val actions: List<Action> = emptyList()
) : MvRxState {
    constructor(target: Entry) : this(parent = target)
}

class ActionCreateViewModel(
    val context: Context,
    state: ActionCreateState
) : MvRxViewModel<ActionCreateState>(state) {

    init {
        buildModel()
    }

    private fun buildModel() = setState { copy(actions = makeActions(parent)) }

    fun <T : Action> execute(actionClass: Class<T>) {
        withState { st ->
            st.actions.firstOrNull { actionClass.isInstance(it) }?.execute(context, GlobalScope)
        }
    }

    fun execute(action: Action) =
        action.execute(context, GlobalScope)

    private fun makeActions(parent: Entry): List<Action> {
        val actions = mutableListOf<Action>()

        actions.add(ActionCreateDocument(parent, ActionCreateDocument.Type.Document))
        actions.add(ActionCreateDocument(parent, ActionCreateDocument.Type.Presentation))
        actions.add(ActionCreateDocument(parent, ActionCreateDocument.Type.Spreadsheet))

        return actions
    }

    companion object : MvRxViewModelFactory<ActionCreateViewModel, ActionCreateState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ActionCreateState
        ): ActionCreateViewModel? {
            return ActionCreateViewModel(viewModelContext.activity(), state)
        }
    }
}

class ActionSheet : BaseMvRxBottomSheet() {
    private val viewModel: ActionCreateViewModel by fragmentViewModel()
    private lateinit var binding: SheetActionCreateBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SheetActionCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun invalidate() = withState(viewModel) { state ->

        binding.recyclerView.withModels {
            state.actions.forEach {
                actionListRow {
                    id(it.title)
                    action(it)
                    clickListener { _ ->
                        viewModel.execute(it)
                        dismiss()
                    }
                }
            }
        }
    }

    companion object {
        fun with(entry: Entry) = ActionSheet().apply {
            arguments = bundleOf(MvRx.KEY_ARG to entry)
        }
    }
}
