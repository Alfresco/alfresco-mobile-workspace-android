package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ActionMoveFilesFolders(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_delete,
    override val title: Int = R.string.action_move_title
) : ActionMove {

    override suspend fun execute(context: Context): Entry {

        val result = ActionMoveFragment.moveItem(context)
        if (!result.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                BrowseRepository().moveNode(entry.id, result)
            }
        } else {
            throw CancellationException("User Cancellation")
        }
        return entry
    }

    override fun copy(_entry: Entry): ActionMove = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(view, anchorView, R.string.action_move_toast, entry.name)
}
