package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.process.models.ResultStartForm
import kotlinx.parcelize.Parcelize

/**
 * Marked as ResponseListStartForm
 */
@Parcelize
data class ResponseListStartForm(
    val id: Int = 0,
    val processDefinitionId: String = "",
    val processDefinitionName: String = "",
    val processDefinitionKey: String = "",
    val fields: List<FieldsData> = emptyList()
) : Parcelable {
    companion object {
        /**
         * returns the ResponseListStartForm obj by using ResultStartForm obj
         */
        fun with(raw: ResultStartForm): ResponseListStartForm {
            return ResponseListStartForm(
                id = raw.id ?: 0,
                processDefinitionId = raw.processDefinitionId ?: "",
                processDefinitionName = raw.processDefinitionName ?: "",
                processDefinitionKey = raw.processDefinitionKey ?: "",
                fields = raw.fields?.map { FieldsData.with(it) } ?: emptyList()
            )
        }
    }
}
