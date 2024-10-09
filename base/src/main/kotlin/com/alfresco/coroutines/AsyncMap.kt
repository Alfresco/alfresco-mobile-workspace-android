package com.alfresco.coroutines

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

suspend fun <T, R> Iterable<T>.asyncMap(f: suspend (T) -> R): List<R> =
    coroutineScope {
        map { async { f(it) } }.awaitAll()
    }

suspend fun <T, R> Iterable<T>.asyncMap(
    limit: Int,
    f: suspend (T) -> R,
): List<R> =
    coroutineScope {
        Semaphore(limit).run {
            map { async { withPermit { f(it) } } }.awaitAll()
        }
    }

suspend fun <T, R> Iterable<T>.asyncMapNotNull(f: suspend (T) -> R?): List<R> =
    coroutineScope {
        mapNotNull { async { f(it) } }.awaitAll().filterNotNull()
    }

suspend fun <T, R> Iterable<T>.asyncMapNotNull(
    limit: Int,
    f: suspend (T) -> R?,
): List<R> =
    coroutineScope {
        Semaphore(limit).run {
            mapNotNull { async { withPermit { f(it) } } }.awaitAll().filterNotNull()
        }
    }
