package com.alfresco.content

import android.util.Log
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Success
import com.alfresco.content.common.BuildConfig
import java.util.concurrent.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

open class MvRxViewModel<S : MvRxState>(
    initialState: S
) : BaseMvRxViewModel<S>(initialState) {

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
}
