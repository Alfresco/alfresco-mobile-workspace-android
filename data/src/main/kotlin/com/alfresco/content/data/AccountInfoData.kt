package com.alfresco.content.data

import com.alfresco.process.models.AccountInfo
import java.time.ZonedDateTime

/**
 * Marked as AccountInfoData
 */
data class AccountInfoData(
    val id: Int? = null,
    val tenantId: Int? = null,
    val created: ZonedDateTime? = null,
    val lastUpdated: ZonedDateTime? = null,
    val alfrescoTenantId: String? = null,
    val repositoryUrl: String? = null,
    val shareUrl: String? = null,
    val name: String? = null,
    val secret: String? = null,
    val authenticationType: String? = null,
    val version: String? = null,
    val sitesFolder: String? = null
) {
    val sourceName = if (id != null && !name.isNullOrEmpty()) "alfresco-$id-${name}Alfresco" else "undefinedAlfresco"

    companion object {
        fun with(raw: AccountInfo): AccountInfoData {
            return AccountInfoData(
                id = raw.id,
                tenantId = raw.tenantId,
                created = raw.created,
                lastUpdated = raw.lastUpdated,
                alfrescoTenantId = raw.alfrescoTenantId,
                repositoryUrl = raw.repositoryUrl,
                shareUrl = raw.shareUrl,
                name = raw.name,
                secret = raw.secret,
                authenticationType = raw.authenticationType,
                version = raw.version,
                sitesFolder = raw.sitesFolder
            )
        }
    }
}
