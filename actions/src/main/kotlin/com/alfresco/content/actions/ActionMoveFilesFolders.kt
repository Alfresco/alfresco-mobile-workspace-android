package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import com.alfresco.content.data.ParentEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Mark as ActionMoveFilesFolders
 */
data class ActionMoveFilesFolders(
    override var entry: Entry,
    override var entries: List<Entry> = emptyList(),
    override val icon: Int = R.drawable.ic_move,
    override val title: Int = R.string.action_move_title,
    override val eventName: EventName = EventName.MoveToFolder,
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

    override suspend fun executeMulti(context: Context): Pair<Entry, List<Entry>> = coroutineScope {
        val entriesObj = entries.toMutableList()
        val result = ActionMoveFragment.moveItem(context, entry)
        if (!result.isNullOrEmpty()) {
            entriesObj.map {
                async(Dispatchers.IO) {
                    BrowseRepository().moveNode(it.id, result)
                }
            }
        } else {
            throw CancellationException("User Cancellation")
        }
        return@coroutineScope Pair(entry, entriesObj)
    }

    override fun copy(_entry: ParentEntry): Action = copy(entry = _entry as Entry)
    override fun copy(_entries: List<Entry>): Action = copy(entries = _entries)

    override fun showToast(view: View, anchorView: View?) {
        if (entries.size > 1) {
            Action.showToast(view, anchorView, R.string.action_move_multiple_toast, entries.size.toString())
        } else {
            Action.showToast(view, anchorView, R.string.action_move_toast, entry.name)
        }
    }
}
