package com.alfresco.content.data

import com.alfresco.process.models.ResultFormVariables

/**
 * Marked as FormVariables
 */
data class FormVariables(
    val id: String? = null,
    val type: String? = null,
    val value: Any? = null
) {
    companion object {
        /**
         * returns the FormVariables using the ResultFormVariables
         */
        fun with(raw: ResultFormVariables): FormVariables {
            return FormVariables(
                id = raw.id,
                type = raw.type,
                value = raw.value
            )
        }
    }
}
