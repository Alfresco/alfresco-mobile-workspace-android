package com.alfresco.content.data.payloads

import android.os.Parcelable
import com.alfresco.content.data.OptionsModel
import com.alfresco.process.models.Fields
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * Marked as FieldsData
 */
@Parcelize
data class FieldsData(
    var fieldType: String = "",
    var id: String = "",
    var name: String = "",
    var message: String = "",
    var type: String = "",
    var value: @RawValue Any? = null,
    var required: Boolean = false,
    var readOnly: Boolean = false,
    var overrideId: Boolean = false,
    var fields: List<FieldsData> = emptyList(),
    var options: List<OptionsModel> = emptyList(),
) : Parcelable {

    companion object {
        /**
         * returns the FieldsData obj by using Fields obj
         */
        fun with(raw: Fields): FieldsData {
            return FieldsData(
                fieldType = raw.fieldType ?: "",
                id = raw.id ?: "",
                name = raw.name ?: "",
                message = raw.message ?: "",
                type = raw.type?.replace("-", "_")?.lowercase() ?: "",
                value = raw.value,
                required = raw.required ?: false,
                readOnly = raw.readOnly ?: false,
                overrideId = raw.overrideId ?: false,
                options = raw.options?.map { OptionsModel.with(it) } ?: emptyList(),
                fields = raw.getFieldMapAsList()?.map { with(it) } ?: emptyList(),
            )
        }
    }
}
