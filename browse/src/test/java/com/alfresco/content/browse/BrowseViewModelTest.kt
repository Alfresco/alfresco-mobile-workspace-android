package com.alfresco.content.browse

import android.content.Context
import com.airbnb.mvrx.test.MavericksTestRule
import com.airbnb.mvrx.withState
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.mockito.MockitoAnnotations

class BrowseViewModelTest {

    private lateinit var viewModel: BrowseViewModel

    private val context: Context = mockk(relaxed = true)

    private var browseRepository: BrowseRepository = mockk(relaxed = true)

    private var offlineRepository: OfflineRepository = mockk(relaxed = true)

    private var testEntries = mutableListOf(
        Entry(id = "1", name = "Entry 1", isSelectedForMultiSelection = false),
        Entry(id = "2", name = "Entry 2", isSelectedForMultiSelection = false)
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `toggleSelection should update the state with entry selection`() {
        viewModel = BrowseViewModel(
            BrowseViewState(
                entries = testEntries,
                path = "",
                nodeId = null,
                moveId = ""
            ),
            context,
            browseRepository,
            offlineRepository
        )
        testEntries.forEach { entry ->
            assertEquals(false, entry.isSelectedForMultiSelection)
        }

        viewModel.toggleSelection(testEntries.first())

        withState(viewModel) { state ->
            assertEquals(1, state.selectedEntries.size)
        }
    }

    @Test
    fun `toggleSelection should update the state with entry selection with in limit`() {

        testEntries = mutableListOf()

        for (i in 0 until 50) {
            testEntries.add(Entry(id = "$i", name = "Entry $i", isSelectedForMultiSelection = false))
        }

        viewModel = BrowseViewModel(
            BrowseViewState(
                entries = testEntries,
                selectedEntries = emptyList(),
                path = "",
                nodeId = null,
                moveId = ""
            ),
            context,
            browseRepository,
            offlineRepository
        )

        withState(viewModel) { state ->
            assertEquals(50, state.entries.size)
        }

        testEntries.forEach { entry ->
            viewModel.toggleSelection(entry)
        }

        withState(viewModel) { state ->
            assertEquals(25, state.selectedEntries.size)
        }
    }

    @Test
    fun `resetMultiSelection should update the state with entries deselection`() {
        viewModel = BrowseViewModel(
            BrowseViewState(
                entries = testEntries,
                path = "",
                nodeId = null,
                moveId = ""
            ),
            context,
            browseRepository,
            offlineRepository
        )
        viewModel.resetMultiSelection()

        withState(viewModel) { state ->
            assertEquals(0, state.selectedEntries.size)
        }
    }

    companion object {
        @JvmField
        @ClassRule
        val mvrxTestRule = MavericksTestRule()
    }
}
