package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.Options
import kotlinx.parcelize.Parcelize

/**
 * Marked as OptionsModel class
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

        /**
         * return the updated OptionsModel obj by using Options obj
         * @param raw
         */
        fun with(raw: Options): OptionsModel {
            return OptionsModel(
                id = raw.id ?: "",
                name = raw.name ?: ""
            )
        }
    }
}
