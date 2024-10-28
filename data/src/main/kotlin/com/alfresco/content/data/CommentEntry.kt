package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.CommentDataEntry
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

/**
 * Marked as CommentEntry class
 */
@Parcelize
data class CommentEntry(
    val id: Int = 0,
    val message: String = "",
    val created: ZonedDateTime? = null,
    val userGroupDetails: UserGroupDetails? = null,
) : Parcelable {
    companion object {
        /**
         * return the CommentEntry obj after converting the data from CommentDataEntry obj
         */
        fun with(data: CommentDataEntry): CommentEntry {
            return CommentEntry(
                id = data.id ?: 0,
                message = data.message ?: "",
                created = data.created,
                userGroupDetails = data.createdBy?.let { UserGroupDetails.with(it) } ?: UserGroupDetails(),
            ).withAssignData()
        }

        /**
         * returns the CommentEntry obj by adding message
         */
        fun addComment(
            message: String,
            userGroupDetails: UserGroupDetails,
        ): CommentEntry {
            return CommentEntry(
                message = message,
                userGroupDetails = userGroupDetails,
                created = ZonedDateTime.now(),
            )
        }
    }

    private fun withAssignData(): CommentEntry {
        val apsUser = TaskRepository().getAPSUser()
        return if (apsUser.id == this.userGroupDetails?.id) {
            copy(
                userGroupDetails = this.userGroupDetails.let { UserGroupDetails.with(it) },
            )
        } else {
            this
        }
    }
}
