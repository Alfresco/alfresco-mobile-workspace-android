package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.AssigneeInfo
import com.alfresco.process.models.TaskDataEntry
import kotlinx.parcelize.Parcelize

/**
 * Marked as TaskEntry class
 */
@Parcelize
data class TaskEntry(
    val id: String = "",
    val name: String = "",
    val assignee: Assignee,
    val priority: String = ""
) : Parcelable {
    companion object {

        /**
         * return the TaskEntry obj using TaskDataEntry
         */
        fun with(data: TaskDataEntry): TaskEntry {
            return TaskEntry(
                id = data.id ?: "",
                name = data.name ?: "",
                assignee = data.assignee?.let { Assignee.with(it) } ?: Assignee(),
                priority = data.priority ?: ""
            )
        }
    }
}

/**
 * Marked as Assignee class
 */
@Parcelize
data class Assignee(
    val id: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = ""
) : Parcelable {

    val name: String
        get() = "$firstName $lastName"

    companion object {

        /**
         * return the Assignee obj using AssigneeInfo
         */
        fun with(assigneeInfo: AssigneeInfo): Assignee {
            return Assignee(
                id = assigneeInfo.id ?: 0,
                firstName = assigneeInfo.firstName ?: "",
                lastName = assigneeInfo.lastName ?: "",
                email = assigneeInfo.email ?: ""
            )
        }
    }
}
