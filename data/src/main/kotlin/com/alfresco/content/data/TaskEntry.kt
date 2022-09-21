package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.TaskDataEntry
import java.time.ZonedDateTime
import kotlinx.parcelize.Parcelize

/**
 * Marked as TaskEntry class
 */
@Parcelize
data class TaskEntry(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val assignee: UserDetails? = null,
    val priority: Int = 0,
    val created: ZonedDateTime? = null,
    val endDate: ZonedDateTime? = null,
    val dueDate: ZonedDateTime? = null,
    val duration: Long? = null,
    val isNewTaskCreated: Boolean = false
) : ParentEntry(), Parcelable {

    companion object {

        /**
         * return the TaskEntry obj using TaskDataEntry
         */
        fun with(data: TaskDataEntry, isNewTaskCreated: Boolean = false): TaskEntry {
            return TaskEntry(
                id = data.id ?: "",
                name = data.name ?: "",
                description = data.description,
                created = data.created,
                assignee = data.assignee?.let { UserDetails.with(it) } ?: UserDetails(),
                priority = data.priority?.toInt() ?: 0,
                endDate = data.endDate,
                dueDate = data.dueDate,
                duration = data.duration,
                isNewTaskCreated = isNewTaskCreated
            )
        }

        fun updateTaskNameDescription(
            data: TaskEntry,
            name: String,
            description: String
        ): TaskEntry {
            return TaskEntry(
                id = data.id,
                name = name,
                description = description,
                created = data.created,
                assignee = data.assignee,
                priority = data.priority,
                endDate = data.endDate,
                dueDate = data.dueDate,
                duration = data.duration
            )
        }
    }
}
