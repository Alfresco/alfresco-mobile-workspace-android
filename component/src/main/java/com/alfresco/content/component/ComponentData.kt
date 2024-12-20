package com.alfresco.content.component

import android.os.Parcelable
import com.alfresco.content.data.Facets
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.data.TaskFilterData
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.models.CategoriesItem
import kotlinx.parcelize.Parcelize

/**
 * Marked as ComponentData class
 */
@Parcelize
data class ComponentData(
    val id: String? = "",
    val name: String? = "",
    val selector: String? = "",
    val query: String? = "",
    val value: String? = "",
    val options: List<ComponentOptions>? = emptyList(),
    val properties: ComponentProperties? = null,
    val selectedName: String = "",
    val selectedQuery: String = "",
    val selectedQueryMap: Map<String, String> = mapOf(),
) : Parcelable {
    companion object {
        /**
         * update the ComponentData obj after getting the result (name and query) from filters
         * @param category
         * @param name
         * @param query
         */
        fun with(
            category: CategoriesItem?,
            name: String,
            query: String,
        ): ComponentData {
            return ComponentData(
                id = category?.id ?: "",
                name = category?.name ?: "",
                selector = category?.component?.selector ?: "",
                options = category?.component?.settings?.options?.map { ComponentOptions.with(it) },
                properties = ComponentProperties.with(category?.component?.settings),
                selectedName = name,
                selectedQuery = query,
            )
        }

        /**
         * update the ComponentData obj after getting the result (name and id) from field options
         * @param fieldsData
         * @param name
         * @param query
         */
        fun with(
            fieldsData: FieldsData,
            name: String,
            query: String,
        ): ComponentData {
            return ComponentData(
                id = fieldsData.id,
                name = fieldsData.name,
                selector = ComponentType.DROPDOWN_RADIO.value,
                options =
                    fieldsData.options.filter { it.id != "empty" }
                        .map { ComponentOptions.withProcess(it) },
                selectedName = name,
                selectedQuery = query,
            )
        }

        /**
         * update the ComponentData obj after getting the result (name and id) from field options
         * @param outcomes
         * @param name
         * @param query
         */
        fun with(
            outcomes: List<OptionsModel>,
            name: String,
            query: String,
            title: String = "",
            selector: String = ComponentType.PROCESS_ACTION.value,
        ): ComponentData {
            return ComponentData(
                name = title,
                selector = selector,
                options = outcomes.map { ComponentOptions.withProcess(it) },
                selectedName = name,
                selectedQuery = query,
            )
        }

        /**
         * update the ComponentData obj after getting the result (name and query) from filters
         * @param facets
         * @param name
         * @param query
         */
        fun with(
            facets: Facets?,
            name: String,
            query: String,
        ): ComponentData {
            return ComponentData(
                id = facets?.hashCode().toString(),
                name = facets?.label ?: "",
                selector = ComponentType.FACETS.value,
                options = facets?.buckets?.map { ComponentOptions.with(it) },
                selectedName = name,
                selectedQuery = query,
            )
        }

        /**
         * Gets the data from TaskFilterData and return as ComponentData obj
         * @param taskFilterData
         */
        fun with(taskFilterData: TaskFilterData): ComponentData {
            return ComponentData(
                id = taskFilterData.id.toString(),
                name = taskFilterData.name,
                selector = taskFilterData.selector,
                query = taskFilterData.query,
                value = taskFilterData.value,
                options = taskFilterData.options?.map { ComponentOptions.with(it) },
                selectedQuery = taskFilterData.selectedQuery,
                selectedName = taskFilterData.selectedName,
                selectedQueryMap = taskFilterData.selectedQueryMap,
            )
        }

        /**
         * update the name and query in the existing ComponentData obj
         * @param componentData
         * @param name
         * @param query
         */
        fun with(
            componentData: ComponentData?,
            name: String,
            query: String,
        ): ComponentData {
            return ComponentData(
                id = componentData?.id,
                name = componentData?.name,
                selector = componentData?.selector,
                query = componentData?.query,
                value = componentData?.value,
                options = componentData?.options,
                properties = componentData?.properties,
                selectedName = name,
                selectedQuery = query,
                selectedQueryMap = componentData?.selectedQueryMap ?: mapOf(),
            )
        }

        /**
         * update the name and queryMap in the existing ComponentData obj
         * @param obj
         * @param selectedName
         * @param selectedQueryMap
         */
        fun with(
            obj: ComponentData?,
            selectedName: String,
            selectedQueryMap: Map<String, String>,
        ): ComponentData {
            return ComponentData(
                id = obj?.id,
                name = obj?.name,
                selector = obj?.selector,
                query = obj?.query,
                value = obj?.value,
                options = obj?.options,
                selectedName = selectedName,
                selectedQueryMap = selectedQueryMap,
            )
        }

        /**
         * update the name, selector and options in the existing ComponentData obj
         * @param taskEntry
         */
        fun with(taskEntry: TaskEntry): ComponentData {
            return ComponentData(
                name = "title_status",
                selector = ComponentType.RADIO.value,
                options =
                    taskEntry.statusOption.filter { it.id != "empty" }
                        .map { ComponentOptions.withTaskStatus(it) },
                selectedName = taskEntry.taskFormStatus ?: "",
                selectedQuery = taskEntry.taskFormStatus ?: "",
            )
        }
    }
}
