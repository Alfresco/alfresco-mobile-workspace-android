package com.alfresco.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T, P> (suspend (P) -> T).asFlow(p: P): Flow<T> =
    flow {
        emit(invoke(p))
    }

fun <T, P1, P2> (suspend (P1, P2) -> T).asFlow(
    p1: P1,
    p2: P2,
): Flow<T> =
    flow {
        emit(invoke(p1, p2))
    }

fun <T, P1, P2, P3> (suspend (P1, P2, P3) -> T).asFlow(
    p1: P1,
    p2: P2,
    p3: P3,
): Flow<T> =
    flow {
        emit(invoke(p1, p2, p3))
    }
