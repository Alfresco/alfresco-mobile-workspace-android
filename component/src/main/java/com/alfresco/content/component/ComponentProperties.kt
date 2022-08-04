package com.alfresco.content.component

import android.os.Parcelable
import com.alfresco.content.models.Options
import com.alfresco.content.models.Settings
import kotlinx.parcelize.Parcelize

@Parcelize
data class ComponentProperties(
    val field: String? = "",
    val pattern: String? = "",
    val placeholder: String? = "",
    val pageSize: Int? = 0,
    val operator: String? = "",
    val options: List<Options>? = emptyList(),
    val min: Int? = 0,
    val max: Int? = 0,
    val step: Int? = 0,
    val thumbLabel: Boolean? = false,
    val format: String? = "",
    val dateFormat: String? = "",
    val maxDate: String? = "",
    val allowUpdateOnChange: Boolean? = false,
    val hideDefaultAction: Boolean? = false,
    val unit: String? = ""
) : Parcelable {
    companion object {
        fun with(settings: Settings?): ComponentProperties {
            return ComponentProperties(
                field = settings?.field ?: "",
                pattern = settings?.pattern ?: "",
                placeholder = settings?.placeholder ?: "",
                pageSize = settings?.pageSize ?: 0,
                operator = settings?.operator ?: "",
                options = settings?.options ?: emptyList(),
                min = settings?.min ?: 0,
                max = settings?.max ?: 0,
                step = settings?.step ?: 0,
                thumbLabel = settings?.thumbLabel ?: false,
                format = settings?.format ?: "",
                dateFormat = settings?.dateFormat ?: "",
                maxDate = settings?.maxDate ?: "",
                allowUpdateOnChange = settings?.allowUpdateOnChange ?: false,
                hideDefaultAction = settings?.hideDefaultAction ?: false,
                unit = settings?.unit ?: ""
            )
        }
    }
}
