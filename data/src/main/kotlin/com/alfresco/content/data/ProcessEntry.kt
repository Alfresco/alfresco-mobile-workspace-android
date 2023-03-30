package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.ProcessInstanceEntry
import java.time.ZonedDateTime
import kotlinx.parcelize.Parcelize

/**
 * Marked as ProcessEntry
 */
@Parcelize
data class ProcessEntry(
    val id: String = "",
    val name: String = "",
    val businessKey: String? = null,
    val processDefinitionId: String? = null,
    val tenantId: String? = null,
    val started: ZonedDateTime? = null,
    val ended: ZonedDateTime? = null,
    val startedBy: UserDetails? = null,
    val processDefinitionName: String? = null,
    val processDefinitionDescription: String? = null,
    val processDefinitionKey: String? = null,
    val processDefinitionCategory: String? = null,
    val processDefinitionVersion: Int? = null,
    val processDefinitionDeploymentId: Int? = null,
    val graphicalNotationDefined: Boolean? = null,
    val startFormDefined: Boolean? = null,
    val suspended: Boolean? = null
) : ParentEntry(), Parcelable {

    companion object {
        fun with(data: ProcessInstanceEntry, apsUser: UserDetails? = null): ProcessEntry {
            val isAssigneeUser = apsUser?.id == data.startedBy?.id
            return ProcessEntry(
                id = data.id ?: "",
                name = data.name ?: "",
                businessKey = data.businessKey,
                processDefinitionId = data.processDefinitionId,
                tenantId = data.tenantId,
                started = data.started,
                ended = data.ended,
                startedBy = if (isAssigneeUser) apsUser?.let { UserDetails.with(it) } else data.startedBy?.let { UserDetails.with(it) } ?: UserDetails(),
                processDefinitionName = data.processDefinitionName,
                processDefinitionDescription = data.processDefinitionDescription,
                processDefinitionKey = data.processDefinitionKey,
                processDefinitionCategory = data.processDefinitionCategory,
                processDefinitionVersion = data.processDefinitionVersion,
                processDefinitionDeploymentId = data.processDefinitionDeploymentId,
                graphicalNotationDefined = data.graphicalNotationDefined,
                startFormDefined = data.startFormDefined,
                suspended = data.suspended
            )
        }
    }
}
