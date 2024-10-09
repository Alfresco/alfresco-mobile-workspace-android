package com.alfresco.content.component

import android.content.Context
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.DATE_FORMAT_1
import com.alfresco.content.DATE_FORMAT_2
import com.alfresco.content.data.kBToByte
import com.alfresco.content.getFormattedDate
import com.alfresco.content.getLocalizedName

/**
 * Mark as ComponentState class
 */
data class ComponentState(
    val parent: ComponentData?,
) : MavericksState

/**
 * Mark as ComponentViewModel class
 */
class ComponentViewModel(
    val context: Context,
    stateChipCreate: ComponentState,
) : MavericksViewModel<ComponentState>(stateChipCreate) {
    var listOptionsData: MutableList<ComponentMetaData> = mutableListOf()
    private var isFacetComponent: Boolean = false
    var onSearchComplete: ((List<ComponentOptions>) -> Unit)? = null
    var searchComponentList: List<ComponentOptions> = emptyList()
    var toValue = ""
    var fromValue = ""
    var fromDate = ""
    var toDate = ""
    var dateFormat = ""
    var delimiters = ""
    var searchQuery = ""
    var priority: Int = -1

    init {
        updateComponentType()
    }

    private fun updateComponentType() =
        withState {
            if (it.parent?.selector == ComponentType.FACETS.value
            ) {
                delimiters = " OR "
                isFacetComponent = true
            } else {
                delimiters = " ${it.parent?.properties?.operator} "
                isFacetComponent = false
            }
        }

    /**
     * update the value for number range
     */
    fun updateFormatNumberRange(isSlider: Boolean) =
        withState {
            if ((fromValue.isNotEmpty() && toValue.isNotEmpty()) && fromValue.toInt() < toValue.toInt()) {
                val nameFormat =
                    if (isSlider) {
                        toValue
                    } else {
                        context.getLocalizedName("$fromValue - $toValue")
                    }
                val queryFormat = "${it.parent?.properties?.field}:[${fromValue.kBToByte()} TO ${toValue.kBToByte()}]"
                updateSingleComponentData(nameFormat, queryFormat)
            } else {
                updateSingleComponentData("", "")
            }
        }

    /**
     * update the value for date range
     */
    fun updateFormatDateRange() =
        withState { state ->

            when (state.parent?.selector) {
                ComponentType.DATE_RANGE.value -> {
                    if ((fromDate.isNotEmpty() && toDate.isNotEmpty())) {
                        val dateFormat = "$fromDate - $toDate"

                        val queryFormat = "${state.parent.properties?.field}:['${fromDate.getFormattedDate(
                            DATE_FORMAT_2,
                            DATE_FORMAT_1,
                        )}' TO '${toDate.getFormattedDate(DATE_FORMAT_2, DATE_FORMAT_1)}']"
                        updateSingleComponentData(dateFormat, queryFormat)
                    } else {
                        updateSingleComponentData("", "")
                    }
                }

                ComponentType.DATE_RANGE_FUTURE.value -> {
                    var dates: Map<String, String> = mapOf()
                    var selectedDateName = ""

                    if (fromDate.isNotEmpty() && toDate.isNotEmpty()) {
                        selectedDateName = "$fromDate - $toDate"
                        dates = mapOf(DUE_AFTER to fromDate, DUE_BEFORE to toDate)
                    } else if (fromDate.isNotEmpty() && toDate.isEmpty()) {
                        selectedDateName = fromDate
                        dates = mapOf(DUE_AFTER to fromDate)
                    } else if (fromDate.isEmpty() && toDate.isNotEmpty()) {
                        selectedDateName = toDate
                        dates = mapOf(DUE_BEFORE to toDate)
                    }

                    setState {
                        copy(parent = ComponentData.with(parent, selectedDateName, dates))
                    }
                }
            }
        }

    /**
     * build single value component data
     */
    fun buildSingleDataModel() =
        withState { state ->
            if (state.parent?.selectedQuery?.isNotEmpty() == true) {
                listOptionsData.add(ComponentMetaData(state.parent.selectedName, state.parent.selectedQuery))
            }
        }

    /**
     * update single selected component option (text)
     */
    fun updateSingleComponentData(name: String) =
        setState {
            val query = if (parent?.properties?.field != null) parent.properties.field + ":'$name'" else name
            copy(parent = ComponentData.with(parent, name, query))
        }

    /**
     * update single selected component option(radio)
     */
    fun updateSingleComponentData(
        name: String,
        query: String,
    ) {
        setState { copy(parent = ComponentData.with(parent, name, query)) }
    }

    /**
     * copy default component data
     */
    fun copyDefaultComponentData() {
        setState {
            val obj = parent?.options?.find { it.default }
            copy(parent = ComponentData.with(parent, context.getLocalizedName(obj?.label ?: ""), obj?.query ?: ""))
        }
    }

    /**
     * build check list model for query and name
     */
    fun buildCheckListModel() =
        withState { state ->
            if (state.parent?.selectedQuery?.isNotEmpty() == true) {
                if (state.parent.selectedQuery.contains(delimiters)) {
                    val arrayQuery = state.parent.selectedQuery.split(delimiters)
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
    fun updateMultipleComponentData(
        name: String,
        query: String,
    ) {
        if (listOptionsData.find { it.query == query } == null) {
            listOptionsData.add(ComponentMetaData(name, query))
        } else {
            val list = listOptionsData.filter { it.query != query }.toMutableList()
            listOptionsData = list
        }

        val selectedName = listOptionsData.joinToString(",") { it.name ?: "" }
        val selectedQuery = listOptionsData.joinToString(delimiters) { it.query ?: "" }

        setState { copy(parent = ComponentData.with(parent, selectedName, selectedQuery)) }
    }

    /**
     * returns the search result from bucket list on the basis of searchText
     */
    fun searchBucket(searchText: String) =
        withState { state ->
            when (state.parent?.selector) {
                ComponentType.FACETS.value -> {
                    requireNotNull(state.parent.options)
                    searchComponentList = state.parent.options.filter { it.label.contains(searchText) }
                    onSearchComplete?.invoke(searchComponentList)
                }
            }
        }

    companion object : MavericksViewModelFactory<ComponentViewModel, ComponentState> {
        const val DUE_BEFORE = "dueBefore"
        const val DUE_AFTER = "dueAfter"

        override fun create(
            viewModelContext: ViewModelContext,
            state: ComponentState,
        ) = ComponentViewModel(viewModelContext.activity(), state)
    }
}
