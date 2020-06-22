package com.alfresco.content.data

import android.content.Context
import com.alfresco.content.apis.FavoritesApi
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FavoritesRepository() {
    private val service: FavoritesApi by lazy {
        SessionManager.requireSession.createService(FavoritesApi::class.java)
    }

    private suspend fun nodes(userId: String): List<Entry> {
        val where = "(EXISTS(target/file) OR EXISTS(target/folder))"
        val include = listOf("path")
        return service.listFavorites(
            userId,
            null,
            25,
            null,
            where,
            include,
            null
        ).list.entries.map { Entry.with(it.entry) }
    }

    suspend fun getFavorites(): Flow<List<Entry>> {
        return flow {
            emit(nodes("-me-"))
        }
    }

    private suspend fun favoritesLibraries(userId: String): List<Entry> {
        val where = "(EXISTS(target/site))"
        return service.listFavorites(
            userId,
            null,
            25,
            null,
            where,
            null,
            null
        ).list.entries.map { Entry.with(it.entry) }
    }

    suspend fun getFavoriteLibraries(): Flow<List<Entry>> {
        return flow {
            emit(favoritesLibraries("-me-"))
        }
    }
}
