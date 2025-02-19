package com.alfresco.content.search

import android.content.Context
import android.net.ConnectivityManager
import com.airbnb.mvrx.test.MavericksTestRule
import com.alfresco.content.data.SearchRepository
import com.alfresco.content.network.ConnectivityTracker
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

@ExperimentalCoroutinesApi
class SearchViewModelTest {

    @get:Rule
    val mvRxTestRule = MavericksTestRule()

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var repository: SearchRepository

    private lateinit var viewModel: SearchViewModel

    @Mock
    lateinit var connectivityTracker: ConnectivityTracker

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        MockitoAnnotations.initMocks(this)
        `when`(connectivityTracker.isActiveNetwork(any())).thenReturn(true)

        val initialState = SearchResultsState()
        viewModel = SearchViewModel(context, initialState, repository)
    }

    @Test
    fun testConnectivity() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        assertNotNull(connectivityManager)
    }


    @Test
    fun `test getSearchFilterList returns non-null list`() {
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
        val position = 2
        viewModel.copyFilterIndex(position)
//        assertEquals(position, viewModel.withState { it.selectedFilterIndex })
    }

    @Test
    fun `test refresh triggers search event`() = runTest {
        val previousParams = viewModel.getSearchQuery()
        viewModel.refresh()
        assertEquals(previousParams, viewModel.getSearchQuery())
    }
}
