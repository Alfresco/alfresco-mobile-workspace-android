package com.alfresco.content.actions.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.Action
import com.alfresco.content.actions.actionListRow
import com.alfresco.content.actions.databinding.SheetActionCreateBinding
import com.alfresco.content.data.Entry
import com.alfresco.ui.BottomSheetDialogFragment
import kotlinx.coroutines.GlobalScope

data class ActionCreateState(
    val parent: Entry,
    val actions: List<Action> = emptyList()
) : MavericksState {
    constructor(target: Entry) : this(parent = target)
}

class ActionCreateViewModel(
    val context: Context,
    state: ActionCreateState
) : MavericksViewModel<ActionCreateState>(state) {

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

        actions.add(ActionUploadPhoto(parent))
        actions.add(ActionCapturePhoto(parent))

        return actions
    }

    companion object : MavericksViewModelFactory<ActionCreateViewModel, ActionCreateState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ActionCreateState
        ) = ActionCreateViewModel(viewModelContext.activity(), state)
    }
}

class ActionSheet : BottomSheetDialogFragment(), MavericksView {
    private val viewModel: ActionCreateViewModel by fragmentViewModel()
    private lateinit var binding: SheetActionCreateBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
            arguments = bundleOf(Mavericks.KEY_ARG to entry)
        }
    }
}
