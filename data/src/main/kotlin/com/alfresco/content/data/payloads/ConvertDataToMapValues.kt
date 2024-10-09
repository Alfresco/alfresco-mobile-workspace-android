package com.alfresco.content.data.payloads

import com.alfresco.content.data.TaskEntry
import com.alfresco.content.data.UserGroupDetails

fun convertModelToMapValues(data: UserGroupDetails?) =
    if (data?.isGroup == true) {
        mapOf<String, Any?>(
            "id" to data.id,
            "name" to data.name,
            "externalId" to data.externalId,
            "status" to data.status,
            "parentGroupId" to data.parentGroupId,
            "groups" to data.groups,
        )
    } else {
        mapOf<String, Any?>(
            "id" to data?.id,
            "firstName" to data?.firstName,
            "lastName" to data?.lastName,
            "email" to data?.email,
        )
    }

fun convertModelToMapValues(data: FieldsData): Map<String, Any?> {
    if (data.value == null) {
        return mapOf()
    }
    val id = data.options.find { it.name == data.value }?.id
    requireNotNull(id)
    return mapOf<String, Any?>(
        "id" to id,
        "name" to data.name,
    )
}

fun convertModelToMapValues(
    data: TaskEntry,
    commentEntry: String? = null,
): Map<String, Any?> =
    if (data.taskFormStatus != null) {
        mapOf(
            "status" to
                mapOf<String, Any?>(
                    "id" to data.taskFormStatus,
                    "name" to data.taskFormStatus,
                ),
            "comment" to (commentEntry ?: data.comment),
        )
    } else {
        mapOf()
    }
