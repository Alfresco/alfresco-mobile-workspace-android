package com.alfresco.content.browse.tasks

import android.content.Context
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.FilterOptions
import com.alfresco.content.data.TaskFilterData
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Mark as FilterCreateState class
 */
data class FilterCreateState(
    val parent: TaskFilterData?
) : MavericksState

/**
 * Mark as ComponentCreateViewModel class
 */
class FilterCreateViewModel(
    val context: Context,
    stateChipCreate: FilterCreateState
) : MavericksViewModel<FilterCreateState>(stateChipCreate) {

    private var listOptionsData: MutableList<FilterMetaData> = mutableListOf()
    var fromDate = ""
    var toDate = ""
    var dateFormat = ""

    /**
     * update the value for number range
     */
    fun updateFormatDateRange() = withState {
        var dates: Map<String, String> = mapOf()
        var selectedDateName = ""

        if (fromDate.isNotEmpty() && toDate.isNotEmpty()) {
            selectedDateName = "$fromDate - $toDate"
            dates = mapOf(DUE_BEFORE to fromDate, DUE_AFTER to toDate)
        } else if (fromDate.isNotEmpty() && toDate.isEmpty()) {
            selectedDateName = fromDate
            dates = mapOf(DUE_BEFORE to fromDate)
        } else if (fromDate.isEmpty() && toDate.isNotEmpty()) {
            selectedDateName = toDate
            dates = mapOf(DUE_AFTER to toDate)
        }

        setState {
            copy(parent = TaskFilterData.with(parent, selectedDateName, dates))
        }
    }

    /**
     * build single value component data
     */
    fun buildSingleDataModel() = withState { state ->
        if (state.parent?.selectedQuery?.isNotEmpty() == true) {
            listOptionsData.add(FilterMetaData(state.parent.selectedName, state.parent.selectedQuery))
        }
    }

    /**
     * update single selected component option (text)
     */
    fun updateSingleComponentData(name: String) =
        setState {
            copy(parent = TaskFilterData.with(parent, selectedName = name, selectedQuery = name))
        }

    /**
     * update single selected component option(radio)
     */
    fun updateSingleComponentData(name: String, query: String) =
        setState { copy(parent = TaskFilterData.with(parent, selectedName = name, selectedQuery = query)) }

    /**
     * copy default component data
     */
    fun copyDefaultComponentData() {

        setState {
            val obj = parent?.options?.get(0)
            copy(parent = TaskFilterData.with(parent, selectedName = obj?.label ?: "", selectedQuery = obj?.query ?: ""))
        }
    }

    /**
     * return true if the component is selected,otherwise false
     */
    fun isOptionSelected(state: FilterCreateState, options: FilterOptions): Boolean {

        val selectedQuery = state.parent?.selectedQuery
        return selectedQuery == options.value
    }

    companion object : MavericksViewModelFactory<FilterCreateViewModel, FilterCreateState> {

        const val DUE_BEFORE = "dueBefore"
        const val DUE_AFTER = "dueAfter"

        override fun create(
            viewModelContext: ViewModelContext,
            state: FilterCreateState
        ) = FilterCreateViewModel(viewModelContext.activity(), state)
    }
}

/**
 * returns formatted date string for query
 */
fun String.getQueryFormat(): String {

    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val date = SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH).parse(this)
    if (date != null)
        return formatter.format(date)

    return this
}
