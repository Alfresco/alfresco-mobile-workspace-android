package com.alfresco.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

public fun <T, P> (suspend (P) -> T).asFlow(p: P): Flow<T> = flow {
    emit(invoke(p))
}
