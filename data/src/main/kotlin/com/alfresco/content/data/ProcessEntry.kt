package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.process.models.ProcessInstanceEntry
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

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
    val startedBy: UserGroupDetails? = null,
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
    val formattedDueDate: String? = null,
    val defaultEntry: Entry? = null,
    val reviewerType: ReviewerType = ReviewerType.OTHER,
    val observeId: String = "",
) : ParentEntry(), Parcelable {

    companion object {

        /**
         * return the ProcessEntry using ProcessInstanceEntry
         */
        fun with(data: ProcessInstanceEntry, apsUser: UserGroupDetails? = null): ProcessEntry {
            val isAssigneeUser = apsUser?.id == data.startedBy?.id
            return ProcessEntry(
                id = data.id ?: "",
                name = data.name ?: "",
                businessKey = data.businessKey,
                processDefinitionId = data.processDefinitionId,
                tenantId = data.tenantId,
                started = data.started,
                ended = data.ended,
                startedBy = if (isAssigneeUser) apsUser?.let { UserGroupDetails.with(it) } else data.startedBy?.let { UserGroupDetails.with(it) } ?: UserGroupDetails(),
                processDefinitionName = data.processDefinitionName,
                processDefinitionDescription = data.processDefinitionDescription,
                processDefinitionKey = data.processDefinitionKey,
                processDefinitionCategory = data.processDefinitionCategory,
                processDefinitionVersion = data.processDefinitionVersion,
                processDefinitionDeploymentId = data.processDefinitionDeploymentId,
                graphicalNotationDefined = data.graphicalNotationDefined,
                startFormDefined = data.startFormDefined,
                suspended = data.suspended,
            )
        }

        /**
         * return the ProcessEntry using RuntimeProcessDefinitionDataEntry
         */
        fun with(data: RuntimeProcessDefinitionDataEntry, entry: Entry?): ProcessEntry {
            return ProcessEntry(
                id = data.id?.toString() ?: "",
                name = data.name ?: "",
                description = data.description ?: "",
                defaultEntry = entry,
            )
        }

        /**
         * return the ProcessEntry using RuntimeProcessDefinitionDataEntry
         */
        fun with(dataObj: ProcessDefinitionDataEntry, processEntry: ProcessEntry?): ProcessEntry {
            return ProcessEntry(
                id = dataObj.id ?: "",
                name = dataObj.name ?: "",
                description = dataObj.description ?: "",
                startFormDefined = dataObj.hasStartForm,
                processDefinitionKey = dataObj.key,
                tenantId = dataObj.tenantId,
                defaultEntry = processEntry?.defaultEntry,
                observeId = buildString { append(dataObj.id).append("::").append(System.currentTimeMillis()) },
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
                priority = priority,
                reviewerType = data.reviewerType,
                observeId = data.observeId,
            )
        }

        /**
         * updating the due date into existing object
         */

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
                priority = data.priority,
                reviewerType = data.reviewerType,
                observeId = data.observeId,
            )
        }

        /**
         * updating the name and description into existing object
         */
        fun updateNameDescription(
            data: ProcessEntry,
            name: String,
            description: String,
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
                priority = data.priority,
                reviewerType = data.reviewerType,
                observeId = data.observeId,
            )
        }

        /**
         * updating the task assignee into existing object
         */
        fun updateAssignee(data: ProcessEntry, assignee: UserGroupDetails): ProcessEntry {
            return ProcessEntry(
                id = data.id,
                name = data.name,
                description = data.description,
                businessKey = data.businessKey,
                processDefinitionId = data.processDefinitionId,
                tenantId = data.tenantId,
                started = data.started,
                ended = data.ended,
                startedBy = assignee,
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
                priority = data.priority,
                reviewerType = data.reviewerType,
                observeId = data.observeId,
            )
        }

        /**
         * update reviewerType into existing ProcessEntry obj
         */
        fun updateReviewerType(data: ProcessEntry, listFields: List<FieldsData>): ProcessEntry {
            var reviewerType: ReviewerType = ReviewerType.PEOPLE
            listFields.forEach {
                if (it.type == ReviewerType.FUNCTIONAL_GROUP.value()) {
                    reviewerType = ReviewerType.FUNCTIONAL_GROUP
                }
            }

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
                priority = data.priority,
                reviewerType = reviewerType,
                observeId = data.observeId,
            )
        }
    }
}

/**
 * Marked as ReviewerType
 */
enum class ReviewerType {
    PEOPLE,
    FUNCTIONAL_GROUP,
    REVIEWGROUPS,
    REVIEWER,
    OTHER,
    ;

    /**
     * returns value of enum in lowercase
     */
    fun value() = name.lowercase()
}
