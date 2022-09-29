package com.alfresco.content.data

import com.alfresco.process.models.ResultUserList

/**
 * Marked as ResponseUserList class
 */
data class ResponseUserList(
    val size: Int = 0,
    val total: Int = 0,
    val start: Int = 0,
    val listUser: List<UserDetails> = emptyList()
) {
    companion object {

        /**
         * return the ResponseUserList obj using ResultUserList
         */
        fun with(raw: ResultUserList): ResponseUserList {
            return ResponseUserList(
                size = raw.size ?: 0,
                total = raw.total ?: 0,
                start = raw.start ?: 0,
                listUser = raw.data?.map { UserDetails.with(it) } ?: emptyList()
            )
        }
    }
}
