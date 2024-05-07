package com.alfresco.content.data.payloads

import android.os.Parcelable
import com.alfresco.content.data.ContentEntry
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.process.models.FieldParams
import com.alfresco.process.models.FieldSource
import com.alfresco.process.models.Fields
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import org.json.JSONObject

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
    var dateDisplayFormat: String? = null,
    var hyperlinkUrl: String? = null,
    var displayText: String? = null,
    var errorData: Pair<Boolean, String> = Pair(false, ""),
) : Parcelable {

    fun getContentList(): List<Entry> {
        return if (((value as? List<*>)?.firstOrNull() is Map<*, *>)) {
            (value as? List<*>)?.map { Gson().fromJson(JSONObject(it as Map<String, ContentEntry>).toString(), ContentEntry::class.java) }?.map { Entry.with(it, id) } ?: emptyList()
        } else {
            (value as? List<*>)?.mapNotNull { it as? Entry } ?: emptyList()
        }
    }

    fun getUserGroupDetails(apsUser: UserGroupDetails?): UserGroupDetails? {
        if (value == null) {
            return null
        }

        val userGroupDetails: UserGroupDetails? = if ((value is Map<*, *>)) {
            Gson().fromJson(JSONObject(value as Map<String, UserGroupDetails>).toString(), UserGroupDetails::class.java)
        } else {
            value as? UserGroupDetails
        }

        val isAssigneeUser = apsUser?.id == userGroupDetails?.id

        if (isAssigneeUser && userGroupDetails != null) {
            return UserGroupDetails.with(userGroupDetails)
        }

        return userGroupDetails
    }

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
                dateDisplayFormat = raw.dateDisplayFormat,
                hyperlinkUrl = raw.hyperlinkUrl,
                displayText = raw.displayText,
            )
        }

        /**
         * returns the FieldsData obj by using Fields obj
         */
        fun withUpdateField(raw: FieldsData, value: Any?, errorData: Pair<Boolean, String>): FieldsData {
            return FieldsData(
                fieldType = raw.fieldType,
                id = raw.id,
                name = raw.name,
                message = raw.message,
                type = raw.type,
                placeHolder = raw.placeHolder,
                value = value,
                minLength = raw.minLength,
                maxLength = raw.maxLength,
                minValue = raw.minValue,
                maxValue = raw.maxValue,
                regexPattern = raw.regexPattern,
                required = raw.required,
                readOnly = raw.readOnly,
                overrideId = raw.overrideId,
                params = raw.params,
                currency = raw.currency,
                enableFractions = raw.enableFractions,
                enablePeriodSeparator = raw.enablePeriodSeparator,
                options = raw.options,
                fields = raw.fields,
                dateDisplayFormat = raw.dateDisplayFormat,
                hyperlinkUrl = raw.hyperlinkUrl,
                displayText = raw.displayText,
                errorData = errorData,
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
    READONLY_TEXT,
    READONLY,
    PEOPLE,
    FUNCTIONAL_GROUP,
    HYPERLINK,
    UPLOAD,
    SELECT_FOLDER,
    ;

    fun value() = name.lowercase()
}

@Parcelize
data class Params(
    val fractionLength: Int = 0,
    val multiple: Boolean = false,
    val fileSource: FileSourceData? = null,
    val field: FieldsData? = null,
    var dateDisplayFormat: String? = null,
) : Parcelable {
    companion object {
        fun with(raw: FieldParams?): Params {
            return Params(
                raw?.fractionLength ?: 0,
                raw?.multiple ?: false,
                FileSourceData.with(raw?.fileSource),
                field = raw?.field?.let { FieldsData.with(it) },
                dateDisplayFormat = raw?.dateDisplayFormat,
            )
        }
    }
}

@Parcelize
data class FileSourceData(
    val serviceId: String = "",
    val name: String = "",
) : Parcelable {
    companion object {
        fun with(
            raw: FieldSource?,
        ): FileSourceData {
            return FileSourceData(raw?.serviceId ?: "", raw?.name ?: "")
        }
    }
}
