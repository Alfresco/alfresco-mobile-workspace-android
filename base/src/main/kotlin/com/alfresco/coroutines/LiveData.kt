package com.alfresco.coroutines

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow

fun <T> LiveData<T>.asFlow() =
    channelFlow {
        send(this@asFlow.value)
        val observer = Observer<T> { t -> offer(t) }
        observeForever(observer)
        awaitClose {
            removeObserver(observer)
        }
    }
