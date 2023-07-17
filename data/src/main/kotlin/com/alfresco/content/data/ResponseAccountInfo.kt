package com.alfresco.content.data

import com.alfresco.process.models.ResultAccountInfo

/**
 * Marked as ResponseAccountInfo
 */
data class ResponseAccountInfo(
    val size: Int? = null,
    val start: Int? = null,
    val total: Int? = null,
    val listAccounts: List<AccountInfoData> = emptyList(),
) {
    companion object {
        /**
         * returns the ResponseAccountInfo obj by using ResultAccountInfo obj
         */
        fun with(raw: ResultAccountInfo): ResponseAccountInfo {
            return ResponseAccountInfo(
                size = raw.size,
                start = raw.start,
                total = raw.total,
                listAccounts = raw.listAccountInfo?.map { AccountInfoData.with(it) } ?: listOf(AccountInfoData()),
            )
        }
    }
}
