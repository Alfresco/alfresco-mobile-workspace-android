package com.alfresco.content.component

import android.os.Parcelable
import com.alfresco.content.data.Buckets
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.models.Options
import kotlinx.parcelize.Parcelize

/**
 * Marked as FilterOptions class
 */
@Parcelize
data class ComponentOptions(
    val label: String = "",
    val query: String = "",
    val value: String = "",
    val default: Boolean = false,
    val count: Int = 0,
) : Parcelable {
    companion object {

        /**
         * return the updated ComponentOptions obj by using OptionsModel obj
         * @param optionsModel
         */
        fun with(optionsModel: OptionsModel): ComponentOptions {
            return ComponentOptions(
                label = optionsModel.label,
                query = optionsModel.query,
                value = optionsModel.value,
                default = optionsModel.default,
            )
        }

        /**
         * return the updated ComponentOptions obj by using OptionsModel obj
         * @param optionsModel
         */
        fun withTaskStatus(optionsModel: OptionsModel): ComponentOptions {
            return ComponentOptions(
                label = optionsModel.name.replace(" ", "_"),
                query = optionsModel.id,
                default = optionsModel.default,
            )
        }

        /**
         * return the updated ComponentOptions obj by using Buckets obj
         * @param bucket
         */
        fun with(bucket: Buckets): ComponentOptions {
            return ComponentOptions(
                label = bucket.label ?: "",
                query = bucket.filterQuery ?: "",
                count = if (bucket.metrics == null) bucket.count ?: 0 else bucket.metrics?.get(0)?.value?.count?.toInt() ?: 0,
            )
        }

        /**
         * return the updated ComponentOptions obj by using Options obj
         * @param options
         */
        fun with(options: Options): ComponentOptions {
            return ComponentOptions(
                label = options.name ?: "",
                query = options.value ?: "",
                default = options.default ?: false,

            )
        }

        /**
         * return the updated ComponentOptions obj by using Options obj
         * @param options
         */
        fun withProcess(options: OptionsModel): ComponentOptions {
            return ComponentOptions(
                label = options.name,
                query = options.id,
                default = options.default,

            )
        }
    }
}
