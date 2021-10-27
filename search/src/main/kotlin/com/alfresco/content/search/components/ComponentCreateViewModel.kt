package com.alfresco.content.search.components

import android.content.Context
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.getLocalizedName
import com.alfresco.content.models.Options
import com.alfresco.content.search.SearchChipCategory

/**
 * Mark as ComponentCreateState class
 */
data class ComponentCreateState(
    val parent: SearchChipCategory
) : MavericksState

/**
 * Mark as ComponentCreateViewModel class
 */
class ComponentCreateViewModel(
    val context: Context,
    stateChipCreate: ComponentCreateState
) : MavericksViewModel<ComponentCreateState>(stateChipCreate) {

    private var listOptionsData: MutableList<ComponentMetaData> = mutableListOf()
    var minRange = ""
    var maxRange = ""

    /**
     * update the value for number range
     */
    fun updateFormatNumberRange() = withState {
        if ((minRange.isNotEmpty() && maxRange.isNotEmpty()) && minRange.toInt() < maxRange.toInt()) {
            val nameFormat = "$minRange - $maxRange"
            val queryFormat = "${it.parent.category.component?.settings?.field}:[$minRange TO $maxRange]"
            updateSingleComponentData(nameFormat, queryFormat)
        } else updateSingleComponentData("", "")
    }

    /**
     * build single value component data
     */
    fun buildSingleDataModel() = withState { state ->
        if (state.parent.selectedQuery.isNotEmpty()) {
            listOptionsData.add(ComponentMetaData(state.parent.selectedName, state.parent.selectedQuery))
        }
    }

    /**
     * update single selected component option (text)
     */
    fun updateSingleComponentData(name: String) =
        setState { copy(parent = getSearchChipCategory(parent, name, parent.category.component?.settings?.field ?: "")) }

    /**
     * update single selected component option(radio)
     */
    fun updateSingleComponentData(name: String, query: String) =
        setState { copy(parent = getSearchChipCategory(parent, context.getLocalizedName(name), query)) }

    /**
     * copy default component data
     */
    fun copyDefaultComponentData() {
        setState {
            val obj = parent.category.component?.settings?.options?.find { it.default ?: false }
            copy(parent = getSearchChipCategory(parent, context.getLocalizedName(obj?.name ?: ""), obj?.value ?: ""))
        }
    }

    private fun getSearchChipCategory(
        parent: SearchChipCategory,
        selectedName: String,
        selectedQuery: String
    ): SearchChipCategory {
        return SearchChipCategory(
            category = parent.category,
            isSelected = parent.isSelected,
            selectedName = selectedName,
            selectedQuery = selectedQuery
        )
    }

    /**
     * return true if the component is selected,otherwise false
     */
    fun isOptionSelected(state: ComponentCreateState, options: Options): Boolean {

        if (state.parent.selectedQuery.isEmpty())
            return options.default ?: false

        val selectedQuery = state.parent.selectedQuery
        if (selectedQuery.contains(",")) {
            selectedQuery.split(",").forEach { query ->
                if (query == options.value)
                    return true
            }
        } else {
            return selectedQuery == options.value
        }
        return false
    }

    /**
     * build check list model for query and name
     */
    fun buildCheckListModel() = withState { state ->

        if (state.parent.selectedQuery.isNotEmpty()) {
            if (state.parent.selectedQuery.contains(",")) {
                val arrayQuery = state.parent.selectedQuery.split(",")
                val arrayName = state.parent.selectedName.split(",")

                arrayQuery.forEachIndexed { index, query ->
                    listOptionsData.add(ComponentMetaData(arrayName[index], query))
                }
            } else {
                listOptionsData.add(ComponentMetaData(state.parent.selectedName, state.parent.selectedQuery))
            }
        }
    }

    /**
     * update multiple component option (check list)
     */
    fun updateMultipleComponentData(name: String, query: String) = withState { state ->

        if (listOptionsData.find { it.query == query } == null) {
            listOptionsData.add(ComponentMetaData(name, query))
        } else {
            val list = listOptionsData.filter { it.query != query }.toMutableList()
            listOptionsData = list
        }

        val selectedName = listOptionsData.joinToString(",") { it.name }
        val selectedQuery = listOptionsData.joinToString(",") { it.query }

        val obj = SearchChipCategory(
            category = state.parent.category,
            isSelected = state.parent.isSelected,
            selectedName = selectedName,
            selectedQuery = selectedQuery
        )

        setState { copy(parent = obj) }
    }

    /**
     * return true if max value valid otherwise false
     */
    fun isMaxValueValid(maxValue: String): Boolean {
        if (maxValue.isEmpty())
            return true

        return if (minRange.isEmpty())
            true
        else
            maxValue.toInt() > minRange.toInt()
    }

    /**
     * return true if min value valid otherwise false
     */
    fun isMinValueValid(minValue: String): Boolean {
        if (minValue.isEmpty())
            return true

        return if (maxRange.isEmpty())
            true
        else
            minValue.toInt() < maxRange.toInt()
    }

    companion object : MavericksViewModelFactory<ComponentCreateViewModel, ComponentCreateState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ComponentCreateState
        ) = ComponentCreateViewModel(viewModelContext.activity(), state)
    }
}
