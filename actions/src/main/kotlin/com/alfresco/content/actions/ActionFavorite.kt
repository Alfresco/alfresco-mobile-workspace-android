package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import com.alfresco.content.data.FavoritesRepository
import com.alfresco.content.data.ParentEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class ActionAddFavorite(
    override val entry: Entry,
    override val entries: List<Entry> = emptyList(),
    override val icon: Int = R.drawable.ic_favorite,
    override val title: Int = R.string.action_add_favorite_title,
    override val eventName: EventName = EventName.AddFavorite,
) : Action {
    private val repository: FavoritesRepository = FavoritesRepository()

    override suspend fun execute(context: Context): Entry {
        repository.addFavorite(entry)
        return entry.copy(isFavorite = true)
    }

    override suspend fun executeMulti(context: Context): Pair<ParentEntry, List<Entry>> =
        coroutineScope {
            val entriesObj = entries.toMutableList()
            entriesObj.map {
                async(Dispatchers.IO) {
                    repository.addFavorite(it)
                }
            }
            return@coroutineScope Pair(entry, entriesObj.map { it.copy(isFavorite = true, isSelectedForMultiSelection = false) })
        }

    override fun copy(_entry: ParentEntry): Action = copy(entry = _entry as Entry)

    override fun copy(_entries: List<Entry>): Action = copy(entries = _entries)

    override fun showToast(
        view: View,
        anchorView: View?,
    ) {
        if (entries.size > 1) {
            Action.showToast(view, anchorView, R.string.action_add_favorite_multiple_toast, entries.size.toString())
        } else {
            Action.showToast(view, anchorView, R.string.action_add_favorite_toast)
        }
    }
}

data class ActionRemoveFavorite(
    override var entry: Entry,
    override val entries: List<Entry> = emptyList(),
    override val icon: Int = R.drawable.ic_favorite_filled,
    override val title: Int = R.string.action_remove_favorite_title,
    override val eventName: EventName = EventName.RemoveFavorite,
) : Action {
    private val repository: FavoritesRepository = FavoritesRepository()

    override suspend fun execute(context: Context): Entry {
        try {
            repository.removeFavorite(entry)
        } catch (ex: KotlinNullPointerException) {
            // no-op. expected for 204
        }
        return entry.copy(isFavorite = false)
    }

    override suspend fun executeMulti(context: Context): Pair<ParentEntry, List<Entry>> =
        coroutineScope {
            val entriesObj = entries.toMutableList()
            try {
                entriesObj.map {
                    async(Dispatchers.IO) {
                        repository.removeFavorite(it)
                    }
                }
            } catch (ex: KotlinNullPointerException) {
                // no-op. expected for 204
                ex.printStackTrace()
            }
            return@coroutineScope Pair(entry, entriesObj.map { it.copy(isFavorite = false, isSelectedForMultiSelection = false) })
        }

    override fun copy(_entry: ParentEntry): Action = copy(entry = _entry as Entry)

    override fun copy(_entries: List<Entry>): Action = copy(entries = _entries)

    override fun showToast(
        view: View,
        anchorView: View?,
    ) {
        if (entries.size > 1) {
            Action.showToast(view, anchorView, R.string.action_remove_favorite_multiple_toast, entries.size.toString())
        } else {
            Action.showToast(view, anchorView, R.string.action_remove_favorite_toast)
        }
    }
}
