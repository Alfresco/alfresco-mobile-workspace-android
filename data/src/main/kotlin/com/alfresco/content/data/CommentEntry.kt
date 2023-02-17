package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.CommentDataEntry
import java.time.ZonedDateTime
import kotlinx.parcelize.Parcelize

/**
 * Marked as CommentEntry class
 */
@Parcelize
data class CommentEntry(
    val id: Int = 0,
    val message: String = "",
    val created: ZonedDateTime? = null,
    val userDetails: UserDetails? = null
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
                userDetails = data.createdBy?.let { UserDetails.with(it) } ?: UserDetails()
            ).withAssignData()
        }

        /**
         * returns the CommentEntry obj by adding message
         */
        fun addComment(message: String, userDetails: UserDetails): CommentEntry {
            return CommentEntry(
                message = message,
                userDetails = userDetails,
                created = ZonedDateTime.now()
            )
        }
    }

    private fun withAssignData(): CommentEntry {
        val apsUser = TaskRepository().getAPSUser()
        return if (apsUser.id == this.userDetails?.id) {
            copy(
                userDetails = this.userDetails.let { UserDetails.with(it) }
            )
        } else this
    }
}
