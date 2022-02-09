package com.alfresco.content.shareextension

import android.content.Context
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext

/**
 * Marked as ExtensionViewModel class
 */
class ExtensionViewModel(
    val context: Context,
    state: ExtensionViewState
) : MavericksViewModel<ExtensionViewState>(state) {

    companion object : MavericksViewModelFactory<ExtensionViewModel, ExtensionViewState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ExtensionViewState
        ) = ExtensionViewModel(viewModelContext.activity(), state)
    }
}
