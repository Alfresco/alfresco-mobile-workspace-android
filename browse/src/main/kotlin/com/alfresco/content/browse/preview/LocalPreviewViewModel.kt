package com.alfresco.content.browse.preview

import android.content.Context
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.actions.ActionOpenWith
import com.alfresco.content.data.Entry
import kotlinx.coroutines.GlobalScope

/**
 * Marked as LocalPreviewState data class
 */
data class LocalPreviewState(
    val entry: Entry?
) : MavericksState {
    constructor(args: LocalPreviewArgs) : this(entry = args.entry)
}

/**
 * Marked as LocalPreviewViewModel class
 */
class LocalPreviewViewModel(
    state: LocalPreviewState,
    val context: Context
) : MavericksViewModel<LocalPreviewState>(state) {

    /**
     * execute to download the files
     */
    fun execute() = withState { state ->
        state.entry?.let {
            val action = ActionOpenWith(it, hasChooser = true)
            action.execute(context, GlobalScope)
        }
    }

    companion object : MavericksViewModelFactory<LocalPreviewViewModel, LocalPreviewState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: LocalPreviewState
        ) =
            // Requires activity context in order to present other fragments
            LocalPreviewViewModel(state, viewModelContext.activity())
    }
}
