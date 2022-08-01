package com.alfresco.content.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class TaskFiltersJson(
    val filters: List<TaskFilterData>
)

@Parcelize
data class TaskFilterData(
    val id: Int? = 0,
    val name: String? = "",
    val selector: String? = "",
    val query: String? = "",
    val value: String? = "",
    val options: List<FilterOptions>? = emptyList(),
    val selectedValue: String? = "",
    val selectedName: String = "",
    val selectedQuery: String = "",
    val selectedQueryMap: Map<String, String> = mapOf(),
    val isSelected: Boolean = false
) : Parcelable {
    companion object {

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
