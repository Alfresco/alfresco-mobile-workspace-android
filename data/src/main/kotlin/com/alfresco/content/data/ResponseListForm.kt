package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.process.models.ResultForm
import kotlinx.parcelize.Parcelize

/**
 * Marked as ResponseListForm
 */
@Parcelize
data class ResponseListForm(
    val id: Int = 0,
    val name: String? = null,
    val processDefinitionId: String = "",
    val processDefinitionName: String = "",
    val processDefinitionKey: String = "",
    val taskId: String? = null,
    val taskName: String? = null,
    val taskDefinitionKey: String? = null,
    val fields: List<FieldsData> = emptyList(),
    val outcomes: List<OptionsModel> = emptyList()
) : Parcelable {
    companion object {
        /**
         * returns the ResponseListForm obj by using ResultForm obj
         */
        fun with(raw: ResultForm): ResponseListForm {
            return ResponseListForm(
                id = raw.id ?: 0,
                name = raw.name,
                processDefinitionId = raw.processDefinitionId ?: "",
                processDefinitionName = raw.processDefinitionName ?: "",
                processDefinitionKey = raw.processDefinitionKey ?: "",
                taskId = raw.taskId ?: "",
                taskName = raw.taskName ?: "",
                taskDefinitionKey = raw.taskDefinitionKey ?: "",
                fields = raw.fields?.map { FieldsData.with(it) } ?: emptyList(),
                outcomes = raw.outcomes?.map { OptionsModel.with(it) } ?: emptyList()
            )
        }
    }
}
