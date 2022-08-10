package com.alfresco.content.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Marked as TaskFiltersJson class
 */
data class TaskFiltersJson(
    val filters: List<TaskFilterData>
)

/**
 * Marked as TaskFilterData class
 */
@Parcelize
data class TaskFilterData(
    val id: Int? = 0,
    val name: String? = "",
    val selector: String? = "",
    val query: String? = "",
    val value: String? = "",
    val options: List<FilterOptions>? = emptyList(),
    val selectedName: String = "",
    val selectedQuery: String = "",
    val selectedQueryMap: Map<String, String> = mapOf(),
    val isSelected: Boolean = false
) : Parcelable {
    companion object {

        /**
         * update to default values on reset
         * @param obj
         */
        fun reset(obj: TaskFilterData): TaskFilterData {
            return TaskFilterData(
                id = obj.id,
                name = obj.name,
                selector = obj.selector,
                query = obj.query,
                value = obj.value,
                options = obj.options
            )
        }

        /**
         * update the TaskFilterData obj after getting the result from filters
         * @param obj
         * @param selectedName
         * @param selectedQuery
         */
        fun with(obj: TaskFilterData?, selectedName: String, selectedQuery: String): TaskFilterData {
            return TaskFilterData(
                id = obj?.id,
                name = obj?.name,
                selector = obj?.selector,
                query = obj?.query,
                value = obj?.value,
                options = obj?.options,
                selectedName = selectedName,
                selectedQuery = selectedQuery,
                isSelected = obj?.isSelected ?: false
            )
        }

        /**
         * update the TaskFilterData obj after getting the result from filters
         * @param obj
         * @param selectedName
         * @param selectedQueryMap
         */
        fun with(obj: TaskFilterData?, selectedName: String, selectedQueryMap: Map<String, String>): TaskFilterData {
            return TaskFilterData(
                id = obj?.id,
                name = obj?.name,
                selector = obj?.selector,
                query = obj?.query,
                value = obj?.value,
                options = obj?.options,
                selectedName = selectedName,
                selectedQueryMap = selectedQueryMap,
                isSelected = obj?.isSelected ?: false
            )
        }

        /**
         * update the TaskFilterData obj after getting the result from filters
         * @param obj
         * @param isSelected
         * @param selectedName
         * @param selectedQuery
         * @param selectedQueryMap
         */
        fun withFilterResult(
            obj: TaskFilterData?,
            isSelected: Boolean,
            selectedName: String,
            selectedQuery: String,
            selectedQueryMap: Map<String, String>
        ): TaskFilterData {
            return TaskFilterData(
                id = obj?.id,
                name = obj?.name,
                selector = obj?.selector,
                query = obj?.query,
                value = obj?.value,
                options = obj?.options,
                selectedName = selectedName,
                selectedQuery = selectedQuery,
                selectedQueryMap = selectedQueryMap,
                isSelected = isSelected
            )
        }

        /**
         * update the if chip selected
         * @param obj
         * @param isSelected
         */
        fun updateData(obj: TaskFilterData, isSelected: Boolean): TaskFilterData {
            return TaskFilterData(
                id = obj.id,
                name = obj.name,
                selector = obj.selector,
                query = obj.query,
                value = obj.value,
                options = obj.options,
                selectedName = obj.selectedName,
                selectedQuery = obj.selectedQuery,
                isSelected = isSelected
            )
        }
    }
}
