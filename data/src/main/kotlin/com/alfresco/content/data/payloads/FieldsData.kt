package com.alfresco.content.data.payloads

import android.os.Parcelable
import com.alfresco.content.data.OptionsModel
import com.alfresco.process.models.FieldParams
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
    var placeHolder: String? = null,
    var value: @RawValue Any? = null,
    var minLength: Int = 0,
    var maxLength: Int = 0,
    var minValue: String? = null,
    var maxValue: String? = null,
    var regexPattern: String? = null,
    var required: Boolean = false,
    var readOnly: Boolean = false,
    var overrideId: Boolean = false,
    var enableFractions: Boolean = false,
    var enablePeriodSeparator: Boolean = false,
    var currency: String? = null,
    var fields: List<FieldsData> = emptyList(),
    var params: Params? = null,
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
                placeHolder = raw.placeholder,
                value = raw.value,
                minLength = raw.minLength,
                maxLength = raw.maxLength,
                minValue = raw.minValue,
                maxValue = raw.maxValue,
                regexPattern = raw.regexPattern,
                required = raw.required ?: false,
                readOnly = raw.readOnly ?: false,
                overrideId = raw.overrideId ?: false,
                params = Params.with(raw.params),
                currency = raw.currency,
                enableFractions = raw.enableFractions ?: false,
                enablePeriodSeparator = raw.enablePeriodSeparator ?: false,
                options = raw.options?.map { OptionsModel.with(it) } ?: emptyList(),
                fields = raw.getFieldMapAsList()?.map { with(it) } ?: emptyList(),
            )
        }
    }
}

enum class FieldType {
    TEXT,
    MULTI_LINE_TEXT,
    INTEGER,
    AMOUNT,
    BOOLEAN,
    DATETIME,
    DATE,
    DROPDOWN,
    RADIO_BUTTONS,
    ;

    fun value() = name.lowercase()
}

@Parcelize
data class Params(val fractionLength: Int = 0) : Parcelable {
    companion object {
        fun with(raw: FieldParams?): Params {
            return Params(raw?.fractionLength ?: 0)
        }
    }
}
