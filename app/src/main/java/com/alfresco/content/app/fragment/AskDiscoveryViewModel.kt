package com.alfresco.content.app.fragment

import android.content.Context
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext


data class AskDiscoveryViewState(val path: String = "") : MavericksState{
    constructor(args: FakeDoorAskDiscoveryArgs) : this(args.path)
}

class AskDiscoveryViewModel(
    viewState: AskDiscoveryViewState,
    val context: Context,
) : MavericksViewModel<AskDiscoveryViewState>(viewState) {


    companion object : MavericksViewModelFactory<AskDiscoveryViewModel, AskDiscoveryViewState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: AskDiscoveryViewState,
        ) = AskDiscoveryViewModel(state, viewModelContext.app())
    }

}