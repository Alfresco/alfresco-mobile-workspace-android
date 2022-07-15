package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.AssigneeInfo
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
    val assignee: Assignee? = null,
    val priority: String = "",
    val created: ZonedDateTime? = null,
    val type: Type = Type.UNKNOWN
) : Parcelable {
    companion object {

        /**
         * return the TaskEntry obj using TaskDataEntry
         */
        fun with(data: TaskDataEntry): TaskEntry {
            return TaskEntry(
                id = data.id ?: "",
                name = data.name ?: "",
                created = data.created,
                assignee = data.assignee?.let { Assignee.with(it) } ?: Assignee(),
                priority = data.priority ?: ""
            )
        }
    }

    /**
     * Marked as Type enum class
     */
    enum class Type {
        GROUP,
        UNKNOWN
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
