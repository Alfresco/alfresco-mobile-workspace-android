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
    var toValue = ""
    var fromValue = ""

    /**
     * update the value for number range
     */
    fun updateFormatNumberRange(isSlider: Boolean) = withState {
        if ((fromValue.isNotEmpty() && toValue.isNotEmpty()) && fromValue.toInt() < toValue.toInt()) {
            val nameFormat = if (isSlider)
                toValue
            else
                context.getLocalizedName("$fromValue - $toValue")
            val queryFormat = "${it.parent.category.component?.settings?.field}:[$fromValue TO $toValue]"
            println("ComponentCreateViewModel.updateFormatNumberRange $nameFormat")
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
        setState { copy(parent = SearchChipCategory.with(parent, name, parent.category.component?.settings?.field ?: "")) }

    /**
     * update single selected component option(radio)
     */
    fun updateSingleComponentData(name: String, query: String) =
        setState { copy(parent = SearchChipCategory.with(parent, name, query)) }

    /**
     * copy default component data
     */
    fun copyDefaultComponentData() {
        setState {
            val obj = parent.category.component?.settings?.options?.find { it.default ?: false }
            copy(parent = SearchChipCategory.with(parent, context.getLocalizedName(obj?.name ?: ""), obj?.value ?: ""))
        }
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

        setState { copy(parent = SearchChipCategory.with(parent, selectedName, selectedQuery)) }
    }

    /**
     * return true if To value valid otherwise false
     */
    fun isToValueValid(to: String): Boolean {
        if (to.isEmpty())
            return true

        return if (fromValue.isEmpty())
            true
        else
            to.toInt() > fromValue.toInt()
    }

    /**
     * return true if from value valid otherwise false
     */
    fun isFromValueValid(from: String): Boolean {
        if (from.isEmpty())
            return true

        return if (toValue.isEmpty())
            true
        else
            from.toInt() < toValue.toInt()
    }

    companion object : MavericksViewModelFactory<ComponentCreateViewModel, ComponentCreateState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ComponentCreateState
        ) = ComponentCreateViewModel(viewModelContext.activity(), state)
    }
}
