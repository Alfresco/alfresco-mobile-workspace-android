package com.alfresco.content.data

import com.alfresco.process.models.ResultList

/**
 * Marked as ResponseList class
 */
data class ResponseList(
    val size: Int,
    val total: Int,
    val start: Int,
    val listTask: List<TaskEntry>
) {
    companion object {

        /**
         * return the ResponseList obj using ResultList
         */
        fun with(raw: ResultList, apsUser: UserDetails): ResponseList {
            return ResponseList(
                size = raw.size ?: 0,
                total = raw.total ?: 0,
                start = raw.start ?: 0,
                listTask = raw.data?.map { TaskEntry.with(it, apsUser) } ?: emptyList()
            )
        }
    }
}
