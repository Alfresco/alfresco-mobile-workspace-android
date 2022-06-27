package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import com.alfresco.content.data.FavoritesRepository

data class ActionAddFavorite(
    override val entry: Entry,
    override val icon: Int = R.drawable.ic_favorite,
    override val title: Int = R.string.action_add_favorite_title,
    override val eventName: EventName = EventName.AddFavorite
) : Action {
    private val repository: FavoritesRepository = FavoritesRepository()

    override suspend fun execute(context: Context): Entry {
        repository.addFavorite(entry)
        return entry.copy(isFavorite = true)
    }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(view, anchorView, R.string.action_add_favorite_toast)
}

data class ActionRemoveFavorite(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_favorite_filled,
    override val title: Int = R.string.action_remove_favorite_title,
    override val eventName: EventName = EventName.RemoveFavorite
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

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(view, anchorView, R.string.action_remove_favorite_toast)
}
