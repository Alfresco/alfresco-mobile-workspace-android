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
    val description: String = "",
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
    val suspended: Boolean? = null,
    var priority: Int = 0,
    val formattedDueDate: String? = null
) : ParentEntry(), Parcelable {

    companion object {

        /**
         * return the ProcessEntry using ProcessInstanceEntry
         */
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

        fun with(data: RuntimeProcessDefinitionDataEntry): ProcessEntry {
            return ProcessEntry(
                id = data.id?.toString() ?: "",
                name = data.name ?: "",
                description = data.description ?: ""
            )
        }

        /**
         * updating the priority into existing object
         */
        fun updatePriority(data: ProcessEntry, priority: Int): ProcessEntry {
            return ProcessEntry(
                id = data.id,
                name = data.name,
                description = data.description,
                businessKey = data.businessKey,
                processDefinitionId = data.processDefinitionId,
                tenantId = data.tenantId,
                started = data.started,
                ended = data.ended,
                startedBy = data.startedBy,
                processDefinitionName = data.processDefinitionName,
                processDefinitionDescription = data.processDefinitionDescription,
                processDefinitionKey = data.processDefinitionKey,
                processDefinitionCategory = data.processDefinitionCategory,
                processDefinitionVersion = data.processDefinitionVersion,
                processDefinitionDeploymentId = data.processDefinitionDeploymentId,
                graphicalNotationDefined = data.graphicalNotationDefined,
                startFormDefined = data.startFormDefined,
                suspended = data.suspended,
                formattedDueDate = data.formattedDueDate,
                priority = priority
            )
        }

        fun updateDueDate(data: ProcessEntry, formattedDate: String?): ProcessEntry {
            return ProcessEntry(
                id = data.id,
                name = data.name,
                description = data.description,
                businessKey = data.businessKey,
                processDefinitionId = data.processDefinitionId,
                tenantId = data.tenantId,
                started = data.started,
                ended = data.ended,
                startedBy = data.startedBy,
                processDefinitionName = data.processDefinitionName,
                processDefinitionDescription = data.processDefinitionDescription,
                processDefinitionKey = data.processDefinitionKey,
                processDefinitionCategory = data.processDefinitionCategory,
                processDefinitionVersion = data.processDefinitionVersion,
                processDefinitionDeploymentId = data.processDefinitionDeploymentId,
                graphicalNotationDefined = data.graphicalNotationDefined,
                startFormDefined = data.startFormDefined,
                suspended = data.suspended,
                formattedDueDate = formattedDate,
                priority = data.priority
            )
        }

        /**
         * updating the name and description into existing object
         */
        fun updateNameDescription(
            data: ProcessEntry,
            name: String,
            description: String
        ): ProcessEntry {
            return ProcessEntry(
                id = data.id,
                name = name,
                description = description,
                businessKey = data.businessKey,
                processDefinitionId = data.processDefinitionId,
                tenantId = data.tenantId,
                started = data.started,
                ended = data.ended,
                startedBy = data.startedBy,
                processDefinitionName = data.processDefinitionName,
                processDefinitionDescription = data.processDefinitionDescription,
                processDefinitionKey = data.processDefinitionKey,
                processDefinitionCategory = data.processDefinitionCategory,
                processDefinitionVersion = data.processDefinitionVersion,
                processDefinitionDeploymentId = data.processDefinitionDeploymentId,
                graphicalNotationDefined = data.graphicalNotationDefined,
                startFormDefined = data.startFormDefined,
                suspended = data.suspended,
                formattedDueDate = data.formattedDueDate,
                priority = data.priority
            )
        }
    }
}
