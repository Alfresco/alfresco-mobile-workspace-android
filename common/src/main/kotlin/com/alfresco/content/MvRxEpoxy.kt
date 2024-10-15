package com.alfresco.content

import androidx.fragment.app.Fragment
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.withState

/*
 * Builds Epoxy models in a background thread.
 */
open class MvRxEpoxyController(
    val buildModelsCallback: EpoxyController.() -> Unit = {},
) : AsyncEpoxyController() {
    override fun buildModels() {
        buildModelsCallback()
    }
}

/**
 * Create a [MvRxEpoxyController] that builds models with the given callback.
 */
fun Fragment.simpleController(buildModels: EpoxyController.() -> Unit) =
    MvRxEpoxyController {
        // Models are built asynchronously, so it is possible that this is called after the fragment
        // is detached under certain race conditions.
        if (view == null || isRemoving) return@MvRxEpoxyController
        buildModels()
    }

/**
 * Create a [MvRxEpoxyController] that builds models with the given callback.
 * When models are built the current state of the viewModel will be provided.
 */
fun <S : MavericksState, A : MavericksViewModel<S>> Fragment.simpleController(
    viewModel: A,
    buildModels: EpoxyController.(state: S) -> Unit,
) = MvRxEpoxyController {
    if (view == null || isRemoving) return@MvRxEpoxyController
    withState(viewModel) { state ->
        buildModels(state)
    }
}
