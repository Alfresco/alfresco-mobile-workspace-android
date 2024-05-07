package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.CommonOptionModel
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
    val default: Boolean = false,
) : Parcelable {

    val outcome: String
        get() = name

    companion object {

        /**
         * return the updated OptionsModel obj by using Options obj
         * @param raw
         */
        fun with(raw: Options): OptionsModel {
            return OptionsModel(
                id = raw.id ?: "",
                name = raw.name ?: "",
            )
        }

        /**
         * return the updated OptionsModel obj by using CommonOptionModel obj
         * @param raw
         */
        fun with(raw: CommonOptionModel): OptionsModel {
            return OptionsModel(
                id = raw.id ?: "",
                name = raw.name ?: "",
            )
        }
    }
}

enum class DefaultOutcomesID {
    DEFAULT_START_WORKFLOW,
    DEFAULT_SAVE,
    DEFAULT_CLAIM,
    DEFAULT_RELEASE,
    DEFAULT_COMPLETE,
    ;
    fun value() = name.lowercase()
}
