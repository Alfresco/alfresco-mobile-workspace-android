package com.alfresco.content.component.searchusergroup

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.ResponseUserGroupList
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.data.UserGroupDetails

/**
 * Mark as SearchUserAndGroupComponentState class
 */

data class SearchUserGroupComponentState(
    val parent: ParentEntry?,
    val requestUser: Async<ResponseUserGroupList> = Uninitialized,
    val listUserGroup: List<UserGroupDetails> = emptyList(),
) : MavericksState {
    constructor(target: ParentEntry) : this(parent = target)

    /**
     * update search user entries after fetch the result from server.
     */
    fun updateUserGroupEntries(response: ResponseUserGroupList?, userGroupDetails: UserGroupDetails): SearchUserGroupComponentState {
        if (response == null) return this
        requireNotNull(parent)
        val filterList = when (parent) {
            is ProcessEntry -> response.listUserGroup.filter { it.id != parent.startedBy?.id }
            else -> response.listUserGroup.filter { it.id != (parent as TaskEntry).assignee?.id }
        }
        var filterUser = filterList.toMutableList()
        if (!response.isGroupSearch) {
            filterUser = filterList.filter { it.id != userGroupDetails.id }.toMutableList()
            filterUser.add(0, UserGroupDetails.with(userGroupDetails))
        }
        return copy(listUserGroup = filterUser)
    }
}
