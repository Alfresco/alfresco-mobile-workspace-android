package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.ResultFormVariables
import kotlinx.parcelize.Parcelize

/**
 * Marked as ResponseFormVariables
 */
@Parcelize
data class ResponseFormVariables(
    val listFormVariables: List<FormVariables> = listOf(),
    val mapFormVariables: Map<String?, String?> = mapOf(),
) : Parcelable {
    companion object {
        /**
         * returns the ResponseFormVariables obj by using ResultFormVariables obj
         */
        fun with(raw: List<ResultFormVariables>): ResponseFormVariables {
            return ResponseFormVariables(
                listFormVariables = raw.map { FormVariables.with(it) },
                mapFormVariables = raw.associate { it.id to it.type },
            )
        }
    }
}
