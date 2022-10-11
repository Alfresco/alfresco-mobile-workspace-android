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
    var priority: Int = 0,
    val created: ZonedDateTime? = null,
    val endDate: ZonedDateTime? = null,
    val dueDate: ZonedDateTime? = null,
    val involvedPeople: List<UserDetails> = listOf(),
    val formattedDueDate: String? = null,
    val duration: Long? = null,
    val isNewTaskCreated: Boolean = false
) : ParentEntry(), Parcelable {

    val localDueDate: String?
        get() = formattedDueDate ?: dueDate?.toLocalDate()?.toString()

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
                involvedPeople = data.involvedPeople?.map { UserDetails.with(it) } ?: emptyList(),
                isNewTaskCreated = isNewTaskCreated
            )
        }

        /**
         * updating the task name and description into existing object
         */
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
                duration = data.duration,
                involvedPeople = data.involvedPeople,
                formattedDueDate = data.formattedDueDate
            )
        }

        /**
         * updating the task due date into existing object
         */
        fun updateTaskDueDate(data: TaskEntry, formattedDueDate: String?, isClearDueDate: Boolean): TaskEntry {
            return TaskEntry(
                id = data.id,
                name = data.name,
                description = data.description,
                created = data.created,
                assignee = data.assignee,
                priority = data.priority,
                endDate = data.endDate,
                dueDate = if (isClearDueDate) null else data.dueDate,
                duration = data.duration,
                involvedPeople = data.involvedPeople,
                formattedDueDate = formattedDueDate
            )
        }

        /**
         * updating the task priority into existing object
         */
        fun updateTaskPriority(data: TaskEntry, priority: Int): TaskEntry {
            return TaskEntry(
                id = data.id,
                name = data.name,
                description = data.description,
                created = data.created,
                assignee = data.assignee,
                priority = priority,
                endDate = data.endDate,
                dueDate = data.dueDate,
                duration = data.duration,
                involvedPeople = data.involvedPeople,
                formattedDueDate = data.formattedDueDate
            )
        }

        /**
         * updating the task assignee into existing object
         */
        fun updateAssignee(data: TaskEntry, assignee: UserDetails): TaskEntry {
            return TaskEntry(
                id = data.id,
                name = data.name,
                description = data.description,
                created = data.created,
                assignee = assignee,
                priority = data.priority,
                endDate = data.endDate,
                dueDate = data.dueDate,
                duration = data.duration,
                involvedPeople = data.involvedPeople,
                formattedDueDate = data.formattedDueDate
            )
        }
    }
}
