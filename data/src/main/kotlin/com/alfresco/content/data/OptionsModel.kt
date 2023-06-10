package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.Options
import kotlinx.parcelize.Parcelize

/**
 * Marked as FilterOptions class
 */
@Parcelize
data class OptionsModel(
    val id: String = "",
    val name: String = "",
    val label: String = "",
    val query: String = "",
    val value: String = "",
    val default: Boolean = false
) : Parcelable {
    companion object {
        fun with(raw: Options): OptionsModel {
            return OptionsModel(
                id = raw.id ?: "",
                name = raw.name ?: ""
            )
        }
    }
}
