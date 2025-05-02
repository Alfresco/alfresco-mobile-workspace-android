package com.alfresco.content.search

import android.content.Context
import android.net.ConnectivityManager
import com.airbnb.mvrx.test.MavericksTestRule
import com.alfresco.content.data.SearchRepository
import com.alfresco.content.network.ConnectivityTracker
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SearchViewModelTest {

    @get:Rule
    val mvRxTestRule = MavericksTestRule()

    private lateinit var viewModel: SearchViewModel

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var repository: SearchRepository

    @MockK
    lateinit var connectivityTracker: ConnectivityTracker

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        val mockConnectivityManager = mockk<ConnectivityManager>(relaxed = true)

        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockConnectivityManager
        every { connectivityTracker.isActiveNetwork(any()) } returns true

        val initialState = SearchResultsState()
        viewModel = SearchViewModel(context, initialState, repository)
    }


    @Test
    fun `test getSearchFilterList returns non-null list`() {
        every { repository.getAppConfig().search } returns emptyList()
        val searchFilters = viewModel.getSearchFilterList()
        assertNotNull(searchFilters)
    }

    @Test
    fun `test setSearchQuery updates params`() {
        val query = "test search"
        viewModel.setSearchQuery(query)
        assertEquals(query, viewModel.getSearchQuery())
    }

    @Test
    fun `test copyFilterIndex updates state`() = runTest {

        val viewModel = SearchViewModel(context, SearchResultsState(), repository)

        val updateState = SearchResultsState(
            contextId = "123456"
        )
        viewModel.defaultFilters(updateState)

        val position = 2
        viewModel.copyFilterIndex(position)

        val newState = viewModel.awaitState()
        assertEquals(position, newState.selectedFilterIndex)
    }

    @Test
    fun `test canSearchOverCurrentNetwork returns true`() {
        every { connectivityTracker.isActiveNetwork(any()) } returns true

        val viewModelResult = viewModel.canSearchOverCurrentNetwork(connectivityTracker)

        assertEquals(true, viewModelResult) // Final assertion
    }


    @Test
    fun `test canSearchOverCurrentNetwork returns false`() {
        every { connectivityTracker.isActiveNetwork(any()) } returns false
        assertEquals(false, viewModel.canSearchOverCurrentNetwork(connectivityTracker))
    }
}
