package com.alfresco.content.shareextension

import android.content.Context
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.BrowseRepository

/**
 * Mark as ExtensionViewState
 */
data class ExtensionViewState(val path: String) : MavericksState {
    constructor(args: ExtensionArgs) : this(args.path)
}

/**
 * Mark as ExtensionViewModel
 */
class ExtensionViewModel(
    state: ExtensionViewState,
    val context: Context
) : MavericksViewModel<ExtensionViewState>(state) {

    /**
     * returns the nodeID for my files
     */
    fun getMyFilesNodeId() = BrowseRepository().myFilesNodeId

    companion object : MavericksViewModelFactory<ExtensionViewModel, ExtensionViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: ExtensionViewState
        ) = ExtensionViewModel(state, viewModelContext.app())
    }
}
