package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.RuntimeProcessDefinitionEntry
import kotlinx.parcelize.Parcelize

/**
 * Marked as RuntimeProcessDefinitionDataEntry
 */
@Parcelize
data class RuntimeProcessDefinitionDataEntry(
    val id: Int? = null,
    val defaultAppId: String? = null,
    val name: String? = null,
    val description: String? = null,
    val modelId: Int? = null,
    val theme: String? = null,
    val icon: String? = null,
    val deploymentId: String? = null,
    val tenantId: Int? = null,
) : ParentEntry(), Parcelable {
    companion object {
        /**
         * return RuntimeProcessDefinitionDataEntry by using RuntimeProcessDefinitionEntry
         */
        fun with(raw: RuntimeProcessDefinitionEntry): RuntimeProcessDefinitionDataEntry {
            return RuntimeProcessDefinitionDataEntry(
                id = raw.id,
                defaultAppId = raw.defaultAppId,
                name = raw.name,
                description = raw.description,
                modelId = raw.modelId,
                theme = raw.theme,
                icon = raw.icon,
                deploymentId = raw.deploymentId,
                tenantId = raw.tenantId,
            )
        }
    }
}
