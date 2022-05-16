package com.alfresco.events

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class EventBus {
    val bus = MutableSharedFlow<Any>()

    suspend fun send(obj: Any) { bus.emit(obj) }

    inline fun <reified T> on() = bus.filter { it is T }.map { it as T }

    companion object {
        val default = EventBus()
    }
}

inline fun <reified T> CoroutineScope.on(
    bus: EventBus = EventBus.default,
    context: CoroutineContext = EmptyCoroutineContext,
    noinline block: suspend (value: T) -> Unit
) = launch(context) {
    bus.on<T>().collect(block)
}

fun <T : Any> CoroutineScope.emit(
    value: T,
    bus: EventBus = EventBus.default,
    context: CoroutineContext = EmptyCoroutineContext
) = launch(context) {
    bus.send(value)
}

inline fun <reified T> CoroutineScope.onLatest(
    bus: EventBus = EventBus.default,
    context: CoroutineContext = EmptyCoroutineContext,
    noinline block: suspend (value: T) -> Unit
) = launch(context) {
    bus.on<T>().collectLatest(block)
}
