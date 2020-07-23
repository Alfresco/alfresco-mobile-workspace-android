package com.alfresco.content

import androidx.fragment.app.Fragment
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.MvRxViewModelProvider
import com.airbnb.mvrx.lifecycleAwareLazy
import kotlin.reflect.KClass

/**
 * Gets or creates a ViewModel scoped to this Fragment. You will get the same instance every time for this Fragment, even
 * through rotation, or other configuration changes.
 *
 * This is based on [fragmentViewModel] solution provided by MvRxExtensions.kt
 */
inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.fragmentViewModelWithArgs(
    viewModelClass: KClass<VM> = VM::class,
    crossinline keyFactory: () -> String = { viewModelClass.java.name },
    crossinline argsProvider: () -> Any?
) where T : Fragment, T : MvRxView = lifecycleAwareLazy(this) {
    MvRxViewModelProvider.get(
        viewModelClass.java,
        S::class.java,
        FragmentViewModelContext(this.requireActivity(), argsProvider(), this),
        keyFactory()
    ).apply { subscribe(this@fragmentViewModelWithArgs, subscriber = { postInvalidate() }) }
}
