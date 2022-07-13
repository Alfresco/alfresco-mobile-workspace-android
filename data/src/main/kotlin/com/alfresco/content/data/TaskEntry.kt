package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.AssigneeInfo
import com.alfresco.process.models.TaskDataEntry
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskEntry(
    val id: String = "",
    val name: String = "",
    val assignee: Assignee,
    val priority: String = ""
) : Parcelable {
    companion object {
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


@Parcelize
data class Assignee(
    val id: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
) : Parcelable {

    val name: String
        get() = "$firstName $lastName"

    companion object {
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