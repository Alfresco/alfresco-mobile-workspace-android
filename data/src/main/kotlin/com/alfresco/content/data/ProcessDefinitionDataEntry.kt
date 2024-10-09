package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.ProcessDefinitionEntry
import kotlinx.parcelize.Parcelize

/**
 * Marked as RuntimeProcessDefinitionDataEntry
 */
@Parcelize
data class ProcessDefinitionDataEntry(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val key: String? = null,
    val category: String? = null,
    val version: Int? = null,
    val deploymentId: String? = null,
    val tenantId: String? = null,
    val hasStartForm: Boolean? = null,
) : ParentEntry(), Parcelable {
    companion object {
        /**
         * return RuntimeProcessDefinitionDataEntry by using RuntimeProcessDefinitionEntry
         */
        fun with(raw: ProcessDefinitionEntry): ProcessDefinitionDataEntry {
            return ProcessDefinitionDataEntry(
                id = raw.id,
                name = raw.name,
                description = raw.description,
                key = raw.key,
                category = raw.category,
                version = raw.version,
                deploymentId = raw.deploymentId,
                tenantId = raw.tenantId,
                hasStartForm = raw.hasStartForm,
            )
        }
    }
}
