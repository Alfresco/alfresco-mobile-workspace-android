package com.alfresco.content.app.component

import android.content.Context
import com.airbnb.mvrx.test.MavericksTestRule
import com.airbnb.mvrx.withState
import com.alfresco.content.DATE_FORMAT_1
import com.alfresco.content.DATE_FORMAT_2
import com.alfresco.content.component.ComponentData
import com.alfresco.content.component.ComponentOptions
import com.alfresco.content.component.ComponentProperties
import com.alfresco.content.component.ComponentState
import com.alfresco.content.component.ComponentType
import com.alfresco.content.component.ComponentViewModel
import com.alfresco.content.data.kBToByte
import com.alfresco.content.getFormattedDate
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.ClassRule
import org.junit.Test

class ComponentViewModelTest {
    private lateinit var viewModel: ComponentViewModel

    private lateinit var state: ComponentState

    private lateinit var componentData: ComponentData

    private val context: Context = mockk(relaxed = true)

    @Test
    fun dateNumberRange_success() {
        // Creating the actual data for ComponentData.
        componentData =
            ComponentData(
                id = "createdDateRange",
                name = "Date Range",
                selector = "date-range",
                options = emptyList(),
                properties =
                    ComponentProperties(
                        field = "cm:created",
                        maxDate = "today",
                        dateFormat = DATE_FORMAT,
                    ),
                selectedName = "",
                selectedQuery = "",
            )

        // Initializing the state
        state = ComponentState(componentData)

        // Initializing the viewModel
        viewModel = ComponentViewModel(context, state)

        // setting From value
        viewModel.fromValue = "32"

        // setting the To value
        viewModel.toValue = "33"

        viewModel.updateFormatNumberRange(false)

        withState(viewModel) {
            assertEquals(it.parent?.selector, ComponentType.DATE_RANGE.value)
            assertEquals(it.parent?.selectedName, "${viewModel.fromValue} - ${viewModel.toValue}")
            assertEquals(
                it.parent?.selectedQuery,
                "${componentData.properties?.field}:[${viewModel.fromValue.kBToByte()} TO ${viewModel.toValue.kBToByte()}]",
            )
        }
    }

    @Test
    fun test_dateNumberRange_empty() {
        // Creating the actual data for ComponentData.
        componentData =
            ComponentData(
                id = "createdDateRange",
                name = "Date Range",
                selector = "date-range",
                options = emptyList(),
                properties =
                    ComponentProperties(
                        field = "cm:created",
                        maxDate = "today",
                        dateFormat = DATE_FORMAT,
                    ),
                selectedName = "",
                selectedQuery = "",
            )

        // Initializing the state
        state = ComponentState(componentData)

        // Initializing the viewModel
        viewModel = ComponentViewModel(context, state)

        // setting From value
        viewModel.fromValue = "40"

        // setting the To value
        viewModel.toValue = "33"

        viewModel.updateFormatNumberRange(false)

        withState(viewModel) {
            assertEquals(it.parent?.selectedName, "")
            assertEquals(it.parent?.selectedQuery, "")
        }
    }

    @Test
    fun sliderNumberRange_success() {
        // Creating the actual data for ComponentData.
        componentData =
            ComponentData(
                id = "createdDateRange",
                name = "Date Range",
                selector = "date-range",
                options = emptyList(),
                properties =
                    ComponentProperties(
                        field = "cm:created",
                        maxDate = "today",
                        dateFormat = DATE_FORMAT,
                    ),
                selectedName = "",
                selectedQuery = "",
            )

        // Initializing the state
        state = ComponentState(componentData)

        // Initializing the viewModel
        viewModel = ComponentViewModel(context, state)

        // setting the To value
        viewModel.fromValue = "0"
        viewModel.toValue = "33"

        viewModel.updateFormatNumberRange(true)

        withState(viewModel) {
            assertEquals(it.parent?.selector, ComponentType.DATE_RANGE.value)
            assertEquals(it.parent?.selectedName, viewModel.toValue)
            assertEquals(
                it.parent?.selectedQuery,
                "${componentData.properties?.field}:[${viewModel.fromValue.kBToByte()} TO ${viewModel.toValue.kBToByte()}]",
            )
        }
    }

    @Test
    fun test_sliderNumberRange_empty() {
        // Creating the actual data for ComponentData.
        componentData =
            ComponentData(
                id = "createdDateRange",
                name = "Date Range",
                selector = "date-range",
                options = emptyList(),
                properties =
                    ComponentProperties(
                        field = "cm:created",
                        maxDate = "today",
                        dateFormat = DATE_FORMAT,
                    ),
                selectedName = "",
                selectedQuery = "",
            )

        // Initializing the state
        state = ComponentState(componentData)

        // Initializing the viewModel
        viewModel = ComponentViewModel(context, state)

        // setting the To value
        viewModel.toValue = ""

        viewModel.updateFormatNumberRange(true)

        withState(viewModel) {
            assertEquals(it.parent?.selectedName, "")
            assertEquals(it.parent?.selectedQuery, "")
        }
    }

    @Test
    fun test_updateFormatDateRange_empty() {
        // Creating the actual data for ComponentData.
        componentData =
            ComponentData(
                id = "createdDateRange",
                name = "Date Range",
                selector = "date-range",
                options = emptyList(),
                properties =
                    ComponentProperties(
                        field = "cm:created",
                        maxDate = "today",
                        dateFormat = DATE_FORMAT,
                    ),
                selectedName = "",
                selectedQuery = "",
            )

        // Initializing the state
        state = ComponentState(componentData)

        // Initializing the viewModel
        viewModel = ComponentViewModel(context, state)

        withState(viewModel) {
            assertEquals(it.parent?.selector, ComponentType.DATE_RANGE.value)

            assertEquals(viewModel.fromDate, "")
            assertEquals(viewModel.toDate, "")
            assertEquals(it.parent?.selectedName, "")
            assertEquals(it.parent?.selectedQuery, "")
        }
    }

    @Test
    fun test_updateFormatDateRange() {
        // Creating the actual data for ComponentData.
        componentData =
            ComponentData(
                id = "createdDateRange",
                name = "Date Range",
                selector = "date-range",
                options = emptyList(),
                properties =
                    ComponentProperties(
                        field = "cm:created",
                        maxDate = "today",
                        dateFormat = DATE_FORMAT,
                    ),
                selectedName = "",
                selectedQuery = "",
            )

        // Initializing the state
        state = ComponentState(componentData)

        // Initializing the viewModel
        viewModel = ComponentViewModel(context, state)

        viewModel.fromDate = "15-Feb-23"
        viewModel.toDate = "20-Feb-23"

        viewModel.updateFormatDateRange()

        withState(viewModel) {
            assertEquals(it.parent?.selector, ComponentType.DATE_RANGE.value)

            assertNotEquals(viewModel.fromDate, "")
            assertNotEquals(viewModel.toDate, "")
            val query =
                "${componentData.properties?.field}:['${viewModel.fromDate.getFormattedDate(
                    DATE_FORMAT_2,
                    DATE_FORMAT_1,
                )}' TO '${viewModel.toDate.getFormattedDate(DATE_FORMAT_2, DATE_FORMAT_1)}']"
            assertEquals(it.parent?.selectedQuery, query)
            assertEquals(it.parent?.selectedName, "${viewModel.fromDate} - ${viewModel.toDate}")
        }
    }

    @Test
    fun test_buildSingleDataModel() {
        // Creating the actual data for ComponentData.
        componentData =
            ComponentData(
                id = "createdDateRange",
                name = "Date Range",
                selector = "date-range",
                options = emptyList(),
                properties =
                    ComponentProperties(
                        field = "cm:created",
                        maxDate = "today",
                        dateFormat = DATE_FORMAT,
                    ),
                selectedName = TEST_NAME,
                selectedQuery = TEST_QUERY,
            )

        // Initializing the state
        state = ComponentState(componentData)

        // Initializing the viewModel
        viewModel = ComponentViewModel(context, state)

        viewModel.buildSingleDataModel()

        withState(viewModel) {
            assertNotEquals(it.parent?.selectedQuery, "")
            val list = viewModel.listOptionsData
            assertEquals(list.size, 1)
        }
    }

    @Test
    fun test_searchBucket() {
        // Creating the actual data for ComponentData.
        componentData =
            ComponentData(
                id = "FacetsID",
                name = "Facets",
                selector = "facets",
                options =
                    listOf(
                        ComponentOptions(
                            label = "test label 1",
                            query = "test query 1",
                        ),
                        ComponentOptions(
                            label = "test label 2",
                            query = TEST_QUERY_2,
                        ),
                    ),
                selectedName = TEST_NAME,
                selectedQuery = TEST_QUERY,
            )

        // Initializing the state
        state = ComponentState(componentData)

        // Initializing the viewModel
        viewModel = ComponentViewModel(context, state)

        viewModel.searchBucket("test label 1")

        withState(viewModel) {
            assertEquals(componentData.selector, ComponentType.FACETS.value)
            assertEquals(componentData.options?.size, 2)
            assertEquals(viewModel.searchComponentList.size, 1)
        }
    }

    @Test
    fun test_searchBucket_empty() {
        // Creating the actual data for ComponentData.
        componentData =
            ComponentData(
                id = "FacetsID",
                name = "Facets",
                selector = "facets",
                options =
                    listOf(
                        ComponentOptions(
                            label = "test label 1",
                            query = "test query 1",
                        ),
                        ComponentOptions(
                            label = "test label 2",
                            query = TEST_QUERY_2,
                        ),
                    ),
                selectedName = TEST_NAME,
                selectedQuery = TEST_QUERY,
            )

        // Initializing the state
        state = ComponentState(componentData)

        // Initializing the viewModel
        viewModel = ComponentViewModel(context, state)

        viewModel.searchBucket("xyz")

        withState(viewModel) {
            assertEquals(componentData.selector, ComponentType.FACETS.value)
            assertEquals(componentData.options?.size, 2)
            assertEquals(viewModel.searchComponentList.size, 0)
        }
    }

    @Test
    fun test_copyDefaultComponentData_radioType_selected() {
        // Creating the actual data for ComponentData.
        componentData =
            ComponentData(
                id = "queryType",
                name = "Radio",
                selector = "radio",
                options =
                    listOf(
                        ComponentOptions(
                            label = "None",
                            query = "",
                            default = true,
                        ),
                        ComponentOptions(
                            label = "All",
                            query = TEST_QUERY_2,
                        ),
                    ),
                selectedName = "",
                selectedQuery = "",
            )

        // Initializing the state
        state = ComponentState(componentData)

        // Initializing the viewModel
        viewModel = ComponentViewModel(context, state)

        viewModel.copyDefaultComponentData()

        withState(viewModel) {
            assertEquals(it.parent?.selectedName, "None")
            assertEquals(it.parent?.query, "")
        }
    }

    @Test
    fun test_copyDefaultComponentData_radioType_notSelected() {
        // Creating the actual data for ComponentData.
        componentData =
            ComponentData(
                id = "queryType",
                name = "Radio",
                selector = "radio",
                options =
                    listOf(
                        ComponentOptions(
                            label = "None",
                            query = "",
                        ),
                        ComponentOptions(
                            label = "All",
                            query = TEST_QUERY_2,
                        ),
                    ),
                selectedName = "",
                selectedQuery = "",
            )

        // Initializing the state
        state = ComponentState(componentData)

        // Initializing the viewModel
        viewModel = ComponentViewModel(context, state)

        viewModel.copyDefaultComponentData()

        withState(viewModel) {
            assertEquals(it.parent?.selectedName, "")
            assertEquals(it.parent?.query, "")
        }
    }

    @Test
    fun test_updateSingleComponentData_name_only() {
        // Creating the actual data for ComponentData.
        componentData =
            ComponentData(
                id = "queryName",
                name = "File Name",
                selector = "text",
                options = emptyList(),
                properties =
                    ComponentProperties(
                        field = "cm:name",
                    ),
                selectedName = "",
                selectedQuery = "",
            )

        // Initializing the state
        state = ComponentState(componentData)

        // Initializing the viewModel
        viewModel = ComponentViewModel(context, state)

        withState(viewModel) {
            assertEquals(it.parent?.selectedName, "")
            assertEquals(it.parent?.selectedQuery, "")
        }
        val searchContent = "Test Content"
        viewModel.updateSingleComponentData(searchContent)

        withState(viewModel) {
            assertEquals(it.parent?.selectedName, searchContent)
            assertEquals(it.parent?.selectedQuery, "${componentData.properties?.field}:'$searchContent'")
        }
    }

    @Test
    fun test_updateSingleComponentData() {
        // Creating the actual data for ComponentData.
        componentData = ComponentData()

        // Initializing the state
        state = ComponentState(componentData)

        // Initializing the viewModel
        viewModel = ComponentViewModel(context, state)

        withState(viewModel) {
            assertEquals(it.parent?.selectedName, "")
            assertEquals(it.parent?.selectedQuery, "")
        }
        val searchContent = "Name Content"
        val queryContent = "Query Content"
        viewModel.updateSingleComponentData(searchContent, queryContent)

        withState(viewModel) {
            assertEquals(it.parent?.selectedName, searchContent)
            assertEquals(it.parent?.selectedQuery, queryContent)
        }
    }

    @Test
    fun test_buildCheckListModel() {
        // Creating the actual data for ComponentData.
        componentData =
            ComponentData(
                id = "queryType",
                name = "check-list",
                selector = "Check List",
                options = emptyList(),
                properties =
                    ComponentProperties(
                        operator = "OR",
                    ),
                selectedName = "data 1,data 2,data 3,data 4",
                selectedQuery = "query 1 OR query 2 OR query 3 OR query4",
            )

        val nameListSize = componentData.selectedName.split(",").size
        val queryListSize = componentData.selectedName.split(componentData.properties?.operator ?: "").size

        // Initializing the state
        state = ComponentState(componentData)

        // Initializing the viewModel
        viewModel = ComponentViewModel(context, state)

        withState(viewModel) {
            assertNotEquals(it.parent?.selectedName, "")
            assertNotEquals(it.parent?.selectedQuery, "")
            assertNotEquals(it.parent?.properties?.operator, null)
            assertEquals(it.parent?.selectedName?.split(",")?.size, nameListSize)
            assertEquals(it.parent?.selectedName?.split(it.parent?.properties?.operator ?: "")?.size, queryListSize)
        }
    }

    companion object {
        @JvmField
        @ClassRule
        val mvrxTestRule = MavericksTestRule()
        private const val TEST_QUERY_2 = "test query 2"
        private const val DATE_FORMAT = "DD-MMM-YY"
        private const val TEST_QUERY = "test-query"
        private const val TEST_NAME = "test-name"
    }
}
