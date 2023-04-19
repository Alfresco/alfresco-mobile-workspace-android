package com.alfresco.content.data

import com.alfresco.process.models.ResultGroupsList
import com.alfresco.process.models.ResultUserList

/**
 * Marked as ResponseUserGroupList class
 */
data class ResponseUserGroupList(
    val size: Int = 0,
    val total: Int = 0,
    val start: Int = 0,
    val isGroupSearch: Boolean = false,
    val listUserGroup: List<UserGroupDetails> = emptyList()
) {
    companion object {

        /**
         * return the ResponseUserList obj using ResultUserList
         */
        fun with(raw: ResultUserList): ResponseUserGroupList {
            return ResponseUserGroupList(
                size = raw.size ?: 0,
                total = raw.total ?: 0,
                start = raw.start ?: 0,
                isGroupSearch = false,
                listUserGroup = raw.data?.map { UserGroupDetails.with(it) } ?: emptyList()
            )
        }

        /**
         * return the ResponseUserList obj using ResultUserList
         */
        fun with(raw: ResultGroupsList): ResponseUserGroupList {
            return ResponseUserGroupList(
                size = raw.size ?: 0,
                total = raw.total ?: 0,
                start = raw.start ?: 0,
                isGroupSearch = true,
                listUserGroup = raw.data?.map { UserGroupDetails.with(it) } ?: emptyList()
            )
        }
    }
}
