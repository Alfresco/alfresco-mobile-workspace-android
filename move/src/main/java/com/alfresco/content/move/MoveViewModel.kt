package com.alfresco.content.move

import android.content.Context
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.BrowseRepository

/**
 * Mark as MoveViewState
 */
data class MoveViewState(val path: String) : MavericksState {
    constructor(args: MoveArgs) : this(args.path)
}

/**
 * Mark as MoveViewModel
 */
class MoveViewModel(
    state: MoveViewState,
    val context: Context
) : MavericksViewModel<MoveViewState>(state) {

    /**
     * returns the nodeID for my files
     */
    fun getMyFilesNodeId() = BrowseRepository().myFilesNodeId

    companion object : MavericksViewModelFactory<MoveViewModel, MoveViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: MoveViewState
        ) = MoveViewModel(state, viewModelContext.app())
    }
}
