package com.alfresco.content.component

import android.os.Parcelable
import com.alfresco.content.data.Buckets
import com.alfresco.content.data.FilterOptions
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
    val count: Int = 0
) : Parcelable {
    companion object {
        fun with(filterOptions: FilterOptions): ComponentOptions {
            return ComponentOptions(
                label = filterOptions.label,
                query = filterOptions.query,
                value = filterOptions.value
            )
        }

        fun with(bucket: Buckets): ComponentOptions {
            return ComponentOptions(
                label = bucket.label ?: "",
                query = bucket.filterQuery ?: "",
                count = if (bucket.metrics == null) bucket.count ?: 0 else bucket.metrics?.get(0)?.value?.count?.toInt() ?: 0
            )
        }

        fun with(options: Options): ComponentOptions {
            return ComponentOptions(
                label = options.name ?: "",
                query = options.value ?: "",
                default = options.default ?: false

            )
        }
    }
}
