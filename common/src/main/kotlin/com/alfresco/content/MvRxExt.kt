package com.alfresco.content

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.ActivityViewModelContext
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelProvider
import com.airbnb.mvrx.RealMavericksStateFactory
import com.airbnb.mvrx._internal
import com.airbnb.mvrx.lifecycleAwareLazy
import kotlin.reflect.KClass
import kotlinx.coroutines.plus

/**
 * Gets or creates a ViewModel scoped to this Fragment. You will get the same instance every time
 * for this Fragment, even through rotation, or other configuration changes.
 *
 * This is based on [com.airbnb.mvrx.fragmentViewModel] solution provided by MvRxExtensions.kt
 */
@OptIn(InternalMavericksApi::class)
inline fun <T, reified VM : MavericksViewModel<S>, reified S : MavericksState> T.fragmentViewModelWithArgs(
    viewModelClass: KClass<VM> = VM::class,
    crossinline keyFactory: () -> String = { viewModelClass.java.name },
    crossinline argsProvider: () -> Any?
) where T : Fragment, T : MavericksView = lifecycleAwareLazy(this) {
    MavericksViewModelProvider.get(
        viewModelClass = viewModelClass.java,
        stateClass = S::class.java,
        viewModelContext = ActivityViewModelContext(
            activity = requireActivity(),
            args = argsProvider()
        ),
        key = keyFactory(),
        initialStateFactory = RealMavericksStateFactory()
    ).apply { _internal(this@fragmentViewModelWithArgs, action = { postInvalidate() }) }
}

/**
 * Gets or creates a ViewModel scoped to this activity. You will get the same instance every time
 * for this activity, even through rotation, or other configuration changes.
 *
 * This is based on [com.airbnb.mvrx.fragmentViewModel] solution provided by MvRxExtensions.kt
 */
@OptIn(InternalMavericksApi::class)
inline fun <T, reified VM : MavericksViewModel<S>, reified S : MavericksState> T.activityViewModel(
    viewModelClass: KClass<VM> = VM::class,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : AppCompatActivity, T : MavericksView = lifecycleAwareLazy(this) {
    MavericksViewModelProvider.get(
        viewModelClass = viewModelClass.java,
        stateClass = S::class.java,
        viewModelContext = ActivityViewModelContext(
            activity = this,
            args = intent.extras?.get(Mavericks.KEY_ARG)
        ),
        key = keyFactory(),
        initialStateFactory = RealMavericksStateFactory()
    ).apply { _internal(this@activityViewModel, action = { postInvalidate() }) }
}
