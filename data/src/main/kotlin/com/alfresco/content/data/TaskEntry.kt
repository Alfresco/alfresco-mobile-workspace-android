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
    val assignee: UserDetails? = null,
    val priority: Int = 0,
    val created: ZonedDateTime? = null,
    val endDate: ZonedDateTime? = null,
    val dueDate: ZonedDateTime? = null,
    val duration: Long? = null
) : ParentEntry(), Parcelable {

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
                priority = data.priority?.toInt() ?: 0,
                endDate = data.endDate,
                dueDate = data.dueDate,
                duration = data.duration
            )
        }
    }
}
