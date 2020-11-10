package com.alfresco.content.data

import com.alfresco.content.apis.FavoritesApi
import com.alfresco.content.models.FavoriteBodyCreate
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FavoritesRepository() {
    private val service: FavoritesApi by lazy {
        SessionManager.requireSession.createService(FavoritesApi::class.java)
    }

    private suspend fun nodes(userId: String, skipCount: Int, maxItems: Int): ResponsePaging {
        val where = "(EXISTS(target/file) OR EXISTS(target/folder))"
        val include = listOf(listOf("path", "allowableOperations").joinToString(","))
        val orderBy = listOf("createdAt DESC")
        return ResponsePaging.with(service.listFavorites(
            userId,
            skipCount,
            maxItems,
            orderBy,
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
        val include = listOf("allowableOperations")
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

    suspend fun getFavoriteLibraries(skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return flow {
            emit(favoritesLibraries("-me-", skipCount, maxItems))
        }
    }

    suspend fun addFavorite(entry: Entry) {
        val key = when (entry.type) {
            Entry.Type.File -> "file"
            Entry.Type.Folder -> "folder"
            Entry.Type.Site -> "site"
            else -> ""
        }

        service.createFavorite(
            "-me-",
            FavoriteBodyCreate(mapOf(key to mapOf("guid" to entry.id))),
            null,
            null
        )
    }

    suspend fun removeFavorite(entry: Entry) {
        service.deleteFavorite("-me-", entry.id)
    }

    suspend fun getFavoriteSite(id: String) =
        Entry.with(service.getFavoriteSite("-me-", id, null).entry)
            .copy(isPartial = false, isFavorite = true)
}
