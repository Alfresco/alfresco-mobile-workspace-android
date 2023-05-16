package com.alfresco.content.data.payloads

import com.alfresco.content.data.UserGroupDetails

/**
 * Marked as ProcessInstancesPayload
 */
data class ProcessInstancesPayload(
    val name: String = "",
    val processDefinitionId: String = "",
    val message: String = "",
    val due: String = "",
    val items: String = "",
    val userGroupDetails: UserGroupDetails? = null,
    val sendEmailNotifications: Boolean = false,
    val priority: String = ""
)
