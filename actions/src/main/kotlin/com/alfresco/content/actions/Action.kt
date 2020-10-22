package com.alfresco.content.actions

import android.util.Log
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.FavoritesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class Action(
    var entry: Entry,
    val icon: Int,
    val title: Int
) {

    abstract suspend fun execute()

    fun execute(
        scope: CoroutineScope
    ) = scope.launch {
        try {
            execute()
            EventBus.default.send(this@Action)
        } catch (ex: Exception) {
            Log.e("Action", ex.message ?: "")
        }
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    class AddFavorite(
        entry: Entry,
        private val repository: FavoritesRepository = FavoritesRepository()
    ) : Action(
        entry,
        R.drawable.ic_favorite,
        R.string.action_add_favorite_title
    ) {
        override suspend fun execute() {
            repository.addFavorite(entry)
            entry = entry.copy(isFavorite = true)
        }
    }

    class RemoveFavorite(
        entry: Entry,
        private val repository: FavoritesRepository = FavoritesRepository()
    ) : Action(
        entry,
        R.drawable.ic_favorite,
        R.string.action_remove_favorite_title
    ) {
        override suspend fun execute() {
            try {
                repository.removeFavorite(entry)
            } catch (ex: KotlinNullPointerException) {
                // no-op. expected for 204
            }
            entry = entry.copy(isFavorite = false)
        }
    }

    class Download(
        entry: Entry
    ) : Action(
        entry,
        R.drawable.ic_download,
        R.string.action_download_title
    ) {
        override suspend fun execute() {
            // TODO:
        }
    }

    class Delete(
        entry: Entry,
        private val repository: BrowseRepository = BrowseRepository()
    ) : Action(
        entry,
        R.drawable.ic_delete,
        R.string.action_delete_title
    ) {
        override suspend fun execute() {
            try {
                repository.deleteEntry(entry)
            } catch (ex: KotlinNullPointerException) {
                // no-op. expected for 204
            }
        }
    }
}
