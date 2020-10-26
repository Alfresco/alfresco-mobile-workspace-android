package com.alfresco.content.actions

import android.util.Log
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.FavoritesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface Action {
    val entry: Entry
    val icon: Int
    val title: Int

    suspend fun execute(): Entry
    fun copy(_entry: Entry): Action

    fun execute(
        scope: CoroutineScope,
        block: (suspend CoroutineScope.(action: Action) -> Unit)? = null
    ) = scope.launch {
        try {
            val newEntry = execute()
            val newAction = copy(newEntry)
            EventBus.default.send(newAction)

            if (block != null) {
                block(newAction)
            }
        } catch (ex: Exception) {
            Log.e("Action", ex.message ?: "")
        }
    }

    data class AddFavorite(
        override val entry: Entry,
        override val icon: Int = R.drawable.ic_favorite,
        override val title: Int = R.string.action_add_favorite_title
    ) : Action {
        private val repository: FavoritesRepository = FavoritesRepository()

        override suspend fun execute(): Entry {
            repository.addFavorite(entry)
            return entry.copy(isFavorite = true)
        }

        override fun copy(_entry: Entry): Action = copy(entry = _entry)
    }

    data class RemoveFavorite(
        override var entry: Entry,
        override val icon: Int = R.drawable.ic_favorite_filled,
        override val title: Int = R.string.action_remove_favorite_title
    ) : Action {
        private val repository: FavoritesRepository = FavoritesRepository()

        override suspend fun execute(): Entry {
            try {
                repository.removeFavorite(entry)
            } catch (ex: KotlinNullPointerException) {
                // no-op. expected for 204
            }
            return entry.copy(isFavorite = false)
        }

        override fun copy(_entry: Entry): Action = copy(entry = _entry)
    }

    data class Download(
        override var entry: Entry,
        override val icon: Int = R.drawable.ic_download,
        override val title: Int = R.string.action_download_title
    ) : Action {

        override suspend fun execute(): Entry {
            // TODO:
            return entry
        }

        override fun copy(_entry: Entry): Action = copy(entry = _entry)
    }

    data class Delete(
        override var entry: Entry,
        override val icon: Int = R.drawable.ic_delete,
        override val title: Int = R.string.action_delete_title
    ) : Action {
        private val repository: BrowseRepository = BrowseRepository()

        override suspend fun execute(): Entry {
            try {
                repository.deleteEntry(entry)
            } catch (ex: KotlinNullPointerException) {
                // no-op. expected for 204
            }
            return entry
        }

        override fun copy(_entry: Entry): Action = copy(entry = _entry)
    }
}
