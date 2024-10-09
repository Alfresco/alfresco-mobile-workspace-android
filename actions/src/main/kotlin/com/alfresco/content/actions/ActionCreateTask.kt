package com.alfresco.content.actions

import android.content.Context
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.EventName
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.data.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Mark as ActionCreateTask class
 */
data class ActionCreateTask(
    override var entry: TaskEntry,
    override val icon: Int = R.drawable.ic_action_create_folder,
    override val title: Int = R.string.action_create_task,
    override val eventName: EventName = EventName.CreateTask,
) : Action {
    override suspend fun execute(context: Context): TaskEntry {
        val result = showCreateTaskDialog(context) ?: throw CancellationException("User Cancellation")
        AnalyticsManager().taskEvent(eventName)
        return TaskRepository().createTask(result.name, result.description)
    }

    private suspend fun showCreateTaskDialog(context: Context) =
        withContext(Dispatchers.Main) {
            suspendCoroutine {
                CreateTaskDialog.Builder(context, false, CreateMetadata(entry.name, entry.description ?: ""))
                    .onSuccess { title, description ->
                        it.resume(CreateMetadata(title, description))
                    }
                    .onCancel { it.resume(null) }
                    .show()
            }
        }

    override fun copy(_entry: ParentEntry): Action = copy(entry = _entry as TaskEntry)
}
