package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.TaskDataEntry
import com.alfresco.process.models.UserInfo
import java.time.ZonedDateTime
import kotlinx.parcelize.Parcelize

/**
 * Marked as TaskEntry class
 */
@Parcelize
data class TaskEntry(
    val id: String = "",
    val name: String = "",
    val assignee: UserDetails? = null,
    val priority: String = "",
    val created: ZonedDateTime? = null,
    val endDate: ZonedDateTime? = null,
    val dueDate: ZonedDateTime? = null,
    val duration: Long? = null
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
                assignee = data.assignee?.let { UserDetails.with(it) } ?: UserDetails(),
                priority = data.priority ?: "",
                endDate = data.endDate,
                dueDate = data.dueDate,
                duration = data.duration
            )
        }
    }
}

/**
 * Marked as Assignee class
 */
@Parcelize
data class UserDetails(
    val id: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = ""
) : Parcelable {

    val name: String
        get() = "$firstName $lastName"

    private val firstNameInitial: String
        get() = if (firstName.isNotEmpty()) firstName.substring(0, 1) else ""

    private val lastNameInitial: String
        get() = if (lastName.isNotEmpty()) lastName.substring(0, 1) else ""

    val nameInitial = firstNameInitial + lastNameInitial

    companion object {

        /**
         * return the UserDetails obj using UserInfo
         */
        fun with(assigneeInfo: UserInfo): UserDetails {
            return UserDetails(
                id = assigneeInfo.id ?: 0,
                firstName = assigneeInfo.firstName ?: "",
                lastName = assigneeInfo.lastName ?: "",
                email = assigneeInfo.email ?: ""
            )
        }
    }
}
