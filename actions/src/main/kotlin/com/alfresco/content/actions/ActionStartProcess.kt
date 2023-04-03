package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import com.alfresco.content.data.FavoritesRepository
import com.alfresco.content.data.ParentEntry

/**
 * Marked as ActionStartProcess
 */
data class ActionStartProcess(
    override val entry: Entry,
    override val icon: Int = R.drawable.ic_favorite,
    override val title: Int = R.string.action_start_workflow,
    override val eventName: EventName = EventName.AddFavorite
) : Action {
    private val repository: FavoritesRepository = FavoritesRepository()

    override suspend fun execute(context: Context): Entry {
        repository.addFavorite(entry)
        return entry.copy(isFavorite = true)
    }

    override fun copy(_entry: ParentEntry): Action = copy(entry = _entry as Entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(view, anchorView, R.string.action_add_favorite_toast)
}
