package com.alfresco.content.data

import com.alfresco.content.apis.FavoritesApi
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FavoritesRepository() {
    private val service: FavoritesApi by lazy {
        SessionManager.requireSession.createService(FavoritesApi::class.java)
    }

    private suspend fun nodes(userId: String, skipCount: Int, maxItems: Int): ResponsePaging {
        val where = "(EXISTS(target/file) OR EXISTS(target/folder))"
        val include = listOf("path")
        return ResponsePaging.with(service.listFavorites(
            userId,
            skipCount,
            maxItems,
            null,
            where,
            include,
            null
        ))
    }

    suspend fun getFavorites(skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return flow {
            emit(nodes("-me-", skipCount, maxItems))
        }
    }

    private suspend fun favoritesLibraries(userId: String, skipCount: Int, maxItems: Int): ResponsePaging {
        val where = "(EXISTS(target/site))"
        return ResponsePaging.with(service.listFavorites(
            userId,
            skipCount,
            maxItems,
            null,
            where,
            null,
            null
        ))
    }

    suspend fun getFavoriteLibraries(skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return flow {
            emit(favoritesLibraries("-me-", skipCount, maxItems))
        }
    }
}
