package com.alfresco.content

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.mvrx.ActivityViewModelContext
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.MvRxViewId
import com.airbnb.mvrx.MvRxViewModelProvider
import com.airbnb.mvrx.lifecycleAwareLazy
import kotlin.reflect.KClass

/**
 * Base class for supporting MvRx activities similar to [com.airbnb.mvrx.BaseMvRxFragment]
 */
abstract class BaseMvRxActivity : AppCompatActivity(), MvRxView {

    private val mvrxViewIdProperty = MvRxViewId()
    final override val mvrxViewId: String by mvrxViewIdProperty

    override fun onCreate(savedInstanceState: Bundle?) {
        mvrxViewIdProperty.restoreFrom(savedInstanceState)
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mvrxViewIdProperty.saveTo(outState)
    }

    inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.activityViewModel(
        viewModelClass: KClass<VM> = VM::class,
        noinline keyFactory: () -> String = { viewModelClass.java.name }
    ) where T : AppCompatActivity, T : MvRxView = lifecycleAwareLazy(this) {
        MvRxViewModelProvider.get(
            viewModelClass.java,
            S::class.java,
            ActivityViewModelContext(this, null),
            keyFactory()
        ).apply { subscribe(this@activityViewModel, subscriber = { postInvalidate() }) }
    }
}
