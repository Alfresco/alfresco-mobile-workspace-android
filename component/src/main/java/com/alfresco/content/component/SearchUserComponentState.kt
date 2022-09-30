package com.alfresco.content.component

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.ResponseUserList
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.data.UserDetails

/**
 * Mark as SearchUserComponentState class
 */

data class SearchUserComponentState(
    val parent: TaskEntry?,
    val requestUser: Async<ResponseUserList> = Uninitialized,
    val listUser: List<UserDetails> = emptyList()
) : MavericksState {
    constructor(target: TaskEntry) : this(parent = target)

    /**
     * update search user entries after fetch the result from server.
     */
    fun updateUserEntries(response: ResponseUserList?, userDetails: UserDetails): SearchUserComponentState {
        if (response == null) return this
        requireNotNull(parent)

        val filterUser = response.listUser.filter { it.id != parent.assignee?.id }.filter { it.id != userDetails.id }.toMutableList()
        filterUser.add(0, UserDetails.with(userDetails))
        return copy(listUser = filterUser)
    }
}
