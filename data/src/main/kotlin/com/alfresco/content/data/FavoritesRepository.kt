package com.alfresco.content.data

import com.alfresco.content.apis.AlfrescoApi
import com.alfresco.content.apis.FavoritesApi
import com.alfresco.content.models.FavoriteBodyCreate
import com.alfresco.content.session.ActionSessionInvalid
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import com.alfresco.content.session.SessionNotFoundException
import com.alfresco.events.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoritesRepository {

    lateinit var session: Session
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        try {
            session = SessionManager.requireSession
        } catch (e: SessionNotFoundException) {
            e.printStackTrace()
            coroutineScope.launch {
                EventBus.default.send(ActionSessionInvalid(true))
            }
        }
    }

    private val service: FavoritesApi by lazy {
        session.createService(FavoritesApi::class.java)
    }

    suspend fun getFavorites(skipCount: Int, maxItems: Int): ResponsePaging {
        val where = "(EXISTS(target/file) OR EXISTS(target/folder))"
        val include = AlfrescoApi.csvQueryParam("path", "allowableOperations")
        val orderBy = listOf("title ASC")
        return ResponsePaging.with(
            service.listFavorites(
                AlfrescoApi.CURRENT_USER,
                skipCount,
                maxItems,
                orderBy,
                where,
                include,
                null,
            ),
        )
    }

    suspend fun getFavoriteLibraries(skipCount: Int, maxItems: Int): ResponsePaging {
        val where = "(EXISTS(target/site))"
        val orderBy = listOf("title ASC")
        return ResponsePaging.with(
            service.listFavorites(
                AlfrescoApi.CURRENT_USER,
                skipCount,
                maxItems,
                orderBy,
                where,
            ),
        )
    }

    suspend fun addFavorite(entry: Entry) {
        val key = when (entry.type) {
            Entry.Type.FILE -> "file"
            Entry.Type.FOLDER -> "folder"
            Entry.Type.SITE -> "site"
            else -> ""
        }

        service.createFavorite(
            AlfrescoApi.CURRENT_USER,
            FavoriteBodyCreate(mapOf(key to mapOf("guid" to entry.id))),
        )
    }

    suspend fun removeFavorite(entry: Entry) {
        service.deleteFavorite(AlfrescoApi.CURRENT_USER, entry.id)
    }

    suspend fun getFavoriteSite(id: String) =
        Entry.with(service.getFavorite(AlfrescoApi.CURRENT_USER, id).entry)

    companion object {
        fun getRepoInstance(): FavoritesRepository? {
            var instance: FavoritesRepository? = null
            try {
                instance = FavoritesRepository()
            } catch (e: SessionNotFoundException) {
                e.printStackTrace()
            }

            return instance
        }
    }
}
