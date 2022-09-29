package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.UserInfo
import kotlinx.parcelize.Parcelize

/**
 * Marked as UserDetails class
 */
@Parcelize
data class UserDetails(
    val id: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val isAssigneeUser: Boolean = false
) : Parcelable {

    val name: String
        get() = if (isAssigneeUser) "Me" else "$firstName $lastName"

    private val firstNameInitial: String
        get() = if (firstName.isNotEmpty()) firstName.substring(0, 1) else ""

    private val lastNameInitial: String
        get() = if (lastName.isNotEmpty()) lastName.substring(0, 1) else ""

    val nameInitial = if (isAssigneeUser) "M" else (firstNameInitial + lastNameInitial).uppercase()

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

        /**
         * return the UserDetails obj using existing the UserDetails obj
         */
        fun with(userDetails: UserDetails): UserDetails {
            return UserDetails(
                id = userDetails.id,
                firstName = userDetails.firstName,
                lastName = userDetails.lastName,
                email = userDetails.email,
                isAssigneeUser = true
            )
        }
    }
}
