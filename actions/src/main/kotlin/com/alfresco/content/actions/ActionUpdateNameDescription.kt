package com.alfresco.content.actions

import android.content.Context
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.EventName
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.TaskEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Mark as ActionUpdateNameDescription class
 */
data class ActionUpdateNameDescription(
    override var entry: ParentEntry,
    override val icon: Int = R.drawable.ic_action_create_folder,
    override val title: Int = R.string.action_update_task_name_description,
    override val eventName: EventName = EventName.None,
) : Action {
    override suspend fun execute(context: Context): ParentEntry {
        val result = showCreateTaskDialog(context) ?: throw CancellationException("User Cancellation")
        AnalyticsManager().taskEvent(eventName)
        return if (entry is ProcessEntry) {
            ProcessEntry.updateNameDescription(entry as ProcessEntry, result.name, result.description)
        } else {
            TaskEntry.updateTaskNameDescription(entry as TaskEntry, result.name, result.description)
        }
    }

    private suspend fun showCreateTaskDialog(context: Context) =
        withContext(Dispatchers.Main) {
            suspendCoroutine {
                CreateTaskDialog.Builder(context, true, getMetaData(entry))
                    .onSuccess { title, description ->
                        it.resume(CreateMetadata(title, description))
                    }
                    .onCancel { it.resume(null) }
                    .show()
            }
        }

    override fun copy(_entry: ParentEntry): Action = copy(entry = _entry)

    private fun getMetaData(entry: ParentEntry): CreateMetadata {
        return when (entry) {
            is ProcessEntry -> {
                CreateMetadata(entry.name, entry.description)
            }
            else -> {
                val data = entry as TaskEntry
                CreateMetadata(data.name, data.description ?: "")
            }
        }
    }
}
