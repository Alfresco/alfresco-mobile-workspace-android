package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.EventName
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.TaskEntry
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Mark as ActionUpdateNameDescription class
 */
data class ActionUpdateNameDescription(
    override var entry: TaskEntry,
    override val icon: Int = R.drawable.ic_action_create_folder,
    override val title: Int = R.string.action_update_task_name_description,
    override val eventName: EventName = EventName.None
) : Action {
    override suspend fun execute(context: Context): TaskEntry {
        val result = showCreateTaskDialog(context) ?: throw CancellationException("User Cancellation")
        AnalyticsManager().taskEvent(eventName)
        return TaskEntry.updateTaskNameDescription(entry, result.name, result.description)
    }

    private suspend fun showCreateTaskDialog(context: Context) = withContext(Dispatchers.Main) {
        suspendCoroutine {
            CreateTaskDialog.Builder(context, true, entry)
                .onSuccess { title, description ->
                    it.resume(CreateMetadata(title, description))
                }
                .onCancel { it.resume(null) }
                .show()
        }
    }

    override fun copy(_entry: ParentEntry): Action = copy(entry = _entry as TaskEntry)

    override fun showToast(view: View, anchorView: View?) {
    }
}
