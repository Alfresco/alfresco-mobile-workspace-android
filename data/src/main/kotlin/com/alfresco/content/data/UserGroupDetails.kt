package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.GroupInfo
import com.alfresco.process.models.UserInfo
import kotlinx.parcelize.Parcelize

/**
 * Marked as UserGroupDetails class
 */
@Parcelize
data class UserGroupDetails(
    val id: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val isAssigneeUser: Boolean = false,
    val groupName: String = "",
    val externalId: Int = 0,
    val status: String = "",
    val parentGroupId: Int = 0,
    val groups: String = "",
    val isGroup: Boolean? = false
) : Parcelable {

    val name: String
        get() = if (isAssigneeUser) "me_title" else "$firstName $lastName"

    private val firstNameInitial: String
        get() = if (firstName.isNotEmpty()) firstName.substring(0, 1) else if (groupName.isNotEmpty()) getGroupInitial(groupName) else ""

    private val lastNameInitial: String
        get() = if (lastName.isNotEmpty()) lastName.substring(0, 1) else ""

    val nameInitial = if (isAssigneeUser) "me_title_initial" else (firstNameInitial + lastNameInitial).uppercase()

    private fun getGroupInitial(groupName: String): String {
        if (!groupName.contains(" "))
            return groupName.substring(0, 1).uppercase()

        val groupNameSplit = groupName.split(" ")

        return (groupNameSplit.first().substring(0, 1) + groupNameSplit.last().substring(0, 1)).uppercase()
    }

    companion object {

        /**
         * return the UserDetails obj using UserInfo
         */
        fun with(assigneeInfo: UserInfo): UserGroupDetails {
            return UserGroupDetails(
                id = assigneeInfo.id ?: 0,
                firstName = assigneeInfo.firstName ?: "",
                lastName = assigneeInfo.lastName ?: "",
                email = assigneeInfo.email ?: ""
            )
        }

        /**
         * return the UserDetails obj using existing the UserDetails obj
         */
        fun with(userGroupDetails: UserGroupDetails): UserGroupDetails {
            return UserGroupDetails(
                id = userGroupDetails.id,
                firstName = userGroupDetails.firstName,
                lastName = userGroupDetails.lastName,
                email = userGroupDetails.email,
                isAssigneeUser = true,
                isGroup = false
            )
        }

        /**
         * return the GroupDetails obj using GroupUserInfo
         */
        fun with(groupInfo: GroupInfo): UserGroupDetails {
            return UserGroupDetails(
                id = groupInfo.id ?: 0,
                groupName = groupInfo.name ?: "",
                externalId = groupInfo.externalId ?: 0,
                status = groupInfo.status ?: "",
                parentGroupId = groupInfo.parentGroupId ?: 0,
                groups = groupInfo.groups ?: "",
                isGroup = true
            )
        }
    }
}
