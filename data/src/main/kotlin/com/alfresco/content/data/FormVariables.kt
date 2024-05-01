package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.ResultFormVariables
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * Marked as FormVariables
 */
@Parcelize
data class FormVariables(
    val id: String? = null,
    val type: String? = null,
    val value: @RawValue Any? = null,
) : Parcelable {
    companion object {
        /**
         * returns the FormVariables using the ResultFormVariables
         */
        fun with(raw: ResultFormVariables): FormVariables {
            return FormVariables(
                id = raw.id,
                type = raw.type,
                value = raw.value,
            )
        }
    }
}
