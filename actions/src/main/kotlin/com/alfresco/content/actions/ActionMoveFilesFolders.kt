package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import com.alfresco.content.data.ParentEntry
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Mark as ActionMoveFilesFolders
 */
data class ActionMoveFilesFolders(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_move,
    override val title: Int = R.string.action_move_title,
    override val eventName: EventName = EventName.MoveToFolder
) : Action {

    override suspend fun execute(context: Context): Entry {

        val result = ActionMoveFragment.moveItem(context, entry)
        if (!result.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                BrowseRepository().moveNode(entry.id, result)
            }
        } else {
            throw CancellationException("User Cancellation")
        }
        return entry
    }

    override fun copy(_entry: ParentEntry): Action = copy(entry = _entry as Entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(view, anchorView, R.string.action_move_toast, entry.name)
}
