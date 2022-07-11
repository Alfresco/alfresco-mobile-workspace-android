package com.alfresco.process.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @property id
 * @property name
 * @property description
 * @property category
 * @property assignee
 * @property created
 * @property dueDate
 * @property endDate
 * @property duration
 * @property priority
 * @property parentTaskId
 * @property parentTaskName
 * @property processInstanceId
 * @property processInstanceName
 * @property processDefinitionId
 * @property processDefinitionName
 * @property processDefinitionKey
 * @property processDefinitionCategory
 * @property processDefinitionVersion
 * @property processDefinitionDeploymentId
 * @property formKey
 * @property processInstanceStartUserId
 * @property initiatorCanCompleteTask
 * @property deactivateUserTaskReassignment
 * @property adhocTaskCanBeReassigned
 * @property taskDefinitionKey
 * @property executionId
 * @property memberOfCandidateGroup
 * @property memberOfCandidateUsers
 * @property managerOfCandidateGroup
 */
@JsonClass(generateAdapter = true)
data class TaskEntry(
    @Json(name = "id") @field:Json(name = "id") var id: Int? = null,
    @Json(name = "name") @field:Json(name = "name") var name: String? = null,
    @Json(name = "description") @field:Json(name = "description") var description: String? = null,
    @Json(name = "category") @field:Json(name = "category") var category: String? = null,
    @Json(name = "assignee") @field:Json(name = "assignee") var assignee: AssigneeInfo? = null,
    @Json(name = "created") @field:Json(name = "created") var created: String? = null,
    @Json(name = "dueDate") @field:Json(name = "dueDate") var dueDate: String? = null,
    @Json(name = "endDate") @field:Json(name = "endDate") var endDate: String? = null,
    @Json(name = "duration") @field:Json(name = "duration") var duration: String? = null,
    @Json(name = "priority") @field:Json(name = "priority") var priority: String? = null,
    @Json(name = "parentTaskId") @field:Json(name = "parentTaskId") var parentTaskId: String? = null,
    @Json(name = "parentTaskName") @field:Json(name = "parentTaskName") var parentTaskName: String? = null,
    @Json(name = "processInstanceId") @field:Json(name = "processInstanceId") var processInstanceId: String? = null,
    @Json(name = "processInstanceName") @field:Json(name = "processInstanceName") var processInstanceName: String? = null,
    @Json(name = "processDefinitionId") @field:Json(name = "processDefinitionId") var processDefinitionId: String? = null,
    @Json(name = "processDefinitionName") @field:Json(name = "processDefinitionName") var processDefinitionName: String? = null,
    @Json(name = "processDefinitionKey") @field:Json(name = "processDefinitionKey") var processDefinitionKey: String? = null,
    @Json(name = "processDefinitionCategory") @field:Json(name = "processDefinitionCategory") var processDefinitionCategory: String? = null,
    @Json(name = "processDefinitionVersion") @field:Json(name = "processDefinitionVersion") var processDefinitionVersion: Int? = null,
    @Json(name = "processDefinitionDeploymentId") @field:Json(name = "processDefinitionDeploymentId") var processDefinitionDeploymentId: String? = null,
    @Json(name = "formKey") @field:Json(name = "formKey") var formKey: String? = null,
    @Json(name = "processInstanceStartUserId") @field:Json(name = "processInstanceStartUserId") var processInstanceStartUserId: String? = null,
    @Json(name = "initiatorCanCompleteTask") @field:Json(name = "initiatorCanCompleteTask") var initiatorCanCompleteTask: Boolean? = null,
    @Json(name = "deactivateUserTaskReassignment") @field:Json(name = "deactivateUserTaskReassignment") var deactivateUserTaskReassignment: Boolean? = null,
    @Json(name = "adhocTaskCanBeReassigned") @field:Json(name = "adhocTaskCanBeReassigned") var adhocTaskCanBeReassigned: Boolean? = null,
    @Json(name = "taskDefinitionKey") @field:Json(name = "taskDefinitionKey") var taskDefinitionKey: String? = null,
    @Json(name = "executionId") @field:Json(name = "executionId") var executionId: String? = null,
    @Json(name = "memberOfCandidateGroup") @field:Json(name = "memberOfCandidateGroup") var memberOfCandidateGroup: Boolean? = null,
    @Json(name = "memberOfCandidateUsers") @field:Json(name = "memberOfCandidateUsers") var memberOfCandidateUsers: Boolean? = null,
    @Json(name = "managerOfCandidateGroup") @field:Json(name = "managerOfCandidateGroup") var managerOfCandidateGroup: Boolean? = null
)
