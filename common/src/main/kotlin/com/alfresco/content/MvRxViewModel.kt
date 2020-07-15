package com.alfresco.content

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.DeliveryMode
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Success
import com.alfresco.content.common.BuildConfig
import io.reactivex.disposables.Disposable
import java.lang.Exception
import kotlin.reflect.KProperty1
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import java.util.concurrent.CancellationException

open class MvRxViewModel<S : MvRxState>(
    initialState: S
) : BaseMvRxViewModel<S>(initialState, debugMode = BuildConfig.DEBUG) {

    private val liveData by lazy(LazyThreadSafetyMode.NONE) {
        MvRxStateLiveData<S> {
            subscribe { value = it }
        }
    }

    protected suspend fun <T> Flow<T>.execute(
        stateReducer: S.(Async<T>) -> S
    ) = execute({ it }, stateReducer)

    protected suspend fun <T, V> Flow<T>.execute(
        mapper: (T) -> V,
        stateReducer: S.(Async<V>) -> S
    ) {
        setState { stateReducer(Loading()) }

        @Suppress("USELESS_CAST")
        return map { Success(mapper(it)) as Async<V> }
            .catch {
                if (BuildConfig.DEBUG) {
                    Log.e(this@MvRxViewModel::class.java.simpleName,
                        "Exception during observe", it)
                }
                emit(Fail(it))
            }
            .collect { setState { stateReducer(it) } }
    }

    protected suspend fun <T, V> Flow<T>.executeOnLatest(
        action: suspend (value: T) -> V,
        stateReducer: S.(Async<V>) -> S
    ) {
        collectLatest {
            setState { stateReducer(Loading()) }
            try {
                val result = action(it)
                setState { stateReducer(Success(result)) }
            } catch (e: CancellationException) {
                // No-op
            } catch (e: Throwable) {
                setState { stateReducer(Fail(e)) }
            }
        }
    }

    fun observeAsLiveData(): LiveData<S> = liveData

    fun <A> selectObserve(
        owner: LifecycleOwner,
        prop1: KProperty1<S, A>,
        deliveryMode: DeliveryMode
    ): LiveData<A> = MvRxStateLiveData {
        selectSubscribe(owner, prop1, deliveryMode) { value = it }
    }

    private class MvRxStateLiveData<T>(
        private val subscribe: MvRxStateLiveData<T>.() -> Disposable
    ) : MutableLiveData<T>() {
        private var disposable: Disposable? = null

        override fun onActive() {
            disposable = subscribe()
        }

        override fun onInactive() {
            val d = disposable
            disposable = null

            if (d != null && !d.isDisposed) {
                d.dispose()
            }
        }
    }
}
