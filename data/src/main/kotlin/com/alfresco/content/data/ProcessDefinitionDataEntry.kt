package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.ProcessDefinitionEntry
import kotlinx.parcelize.Parcelize

/**
 * Marked as ProcessDefinitionDataEntry
 */
@Parcelize
data class ProcessDefinitionDataEntry(
    val id: Int? = null,
    val defaultAppId: String? = null,
    val name: String? = null,
    val description: String? = null,
    val modelId: Int? = null,
    val theme: String? = null,
    val icon: String? = null,
    val deploymentId: String? = null,
    val tenantId: Int? = null
) : ParentEntry(), Parcelable {
    companion object {

        /**
         * return ProcessDefinitionDataEntry by using ProcessDefinitionEntry
         */
        fun with(raw: ProcessDefinitionEntry): ProcessDefinitionDataEntry {
            return ProcessDefinitionDataEntry(
                id = raw.id,
                defaultAppId = raw.defaultAppId,
                name = raw.name,
                description = raw.description,
                modelId = raw.modelId,
                theme = raw.theme,
                icon = raw.icon,
                deploymentId = raw.deploymentId,
                tenantId = raw.tenantId
            )
        }
    }
}
