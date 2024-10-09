package com.alfresco.content.actions

import android.content.Context
import com.airbnb.mvrx.test.MavericksTestRule
import com.airbnb.mvrx.withState
import com.alfresco.content.data.ContextualActionData
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineStatus
import com.alfresco.content.data.Settings
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.UUID

@ExperimentalCoroutinesApi
class ContextualActionsViewModelTest {
    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var settings: Settings

    @Mock
    private lateinit var mockSession: Session // Mock the Session class

    // Helper function to create a list of sample entries
    private fun createEntries(vararg entries: Entry): List<Entry> {
        return entries.toList()
    }

    @Before
    fun setup() {
        // Set up the mock session in the SessionManager
        MockitoAnnotations.openMocks(this)

        SessionManager.currentSession = mockSession
    }

    @Test
    fun `test makeMultiActions and returns ActionAddFavorite`() {
        val entries =
            createEntries(
                Entry(
                    id = UUID.randomUUID().toString(),
                    isFavorite = false,
                    type = Entry.Type.FILE,
                    canDelete = false,
                    offlineStatus = OfflineStatus.UNDEFINED,
                    isOffline = false,
                ),
                Entry(
                    id = UUID.randomUUID().toString(),
                    isFavorite = false,
                    type = Entry.Type.FILE,
                    canDelete = false,
                    offlineStatus = OfflineStatus.UNDEFINED,
                    isOffline = false,
                ),
                Entry(
                    id = UUID.randomUUID().toString(),
                    isFavorite = false,
                    type = Entry.Type.FILE,
                    canDelete = false,
                    offlineStatus = OfflineStatus.UNDEFINED,
                    isOffline = false,
                ),
                Entry(
                    id = UUID.randomUUID().toString(),
                    isFavorite = false,
                    type = Entry.Type.FILE,
                    canDelete = false,
                    offlineStatus = OfflineStatus.UNDEFINED,
                    isOffline = false,
                ),
                Entry(
                    id = UUID.randomUUID().toString(),
                    isFavorite = false,
                    type = Entry.Type.FILE,
                    canDelete = false,
                    offlineStatus = OfflineStatus.UNDEFINED,
                    isOffline = false,
                ),
            )

        val initialState = ContextualActionsState(ContextualActionData(entries = entries, isMultiSelection = true))
        val viewModel = ContextualActionsViewModel(initialState, mockContext, settings)

        withState(viewModel) { newState ->
            viewModel.makeMultiActions(newState)
            assertEquals(1, newState.actions?.size ?: 0)

            newState.actions?.forEach {
                assertEquals(true, it is ActionAddFavorite)
            }
        }
    }

    @Test
    fun `test makeMultiActions and returns ActionRemoveFavorite`() {
        val entries =
            createEntries(
                Entry(
                    id = UUID.randomUUID().toString(),
                    isFavorite = true,
                    type = Entry.Type.FILE,
                    canDelete = false,
                    offlineStatus = OfflineStatus.UNDEFINED,
                    isOffline = false,
                ),
                Entry(
                    id = UUID.randomUUID().toString(),
                    isFavorite = true,
                    type = Entry.Type.FILE,
                    canDelete = false,
                    offlineStatus = OfflineStatus.UNDEFINED,
                    isOffline = false,
                ),
                Entry(
                    id = UUID.randomUUID().toString(),
                    isFavorite = true,
                    type = Entry.Type.FILE,
                    canDelete = false,
                    offlineStatus = OfflineStatus.UNDEFINED,
                    isOffline = false,
                ),
                Entry(
                    id = UUID.randomUUID().toString(),
                    isFavorite = true,
                    type = Entry.Type.FILE,
                    canDelete = false,
                    offlineStatus = OfflineStatus.UNDEFINED,
                    isOffline = false,
                ),
                Entry(
                    id = UUID.randomUUID().toString(),
                    isFavorite = true,
                    type = Entry.Type.FILE,
                    canDelete = false,
                    offlineStatus = OfflineStatus.UNDEFINED,
                    isOffline = false,
                ),
            )

        val initialState = ContextualActionsState(ContextualActionData(entries = entries, isMultiSelection = true))
        val viewModel = ContextualActionsViewModel(initialState, mockContext, settings)

        withState(viewModel) { newState ->
            viewModel.makeMultiActions(newState)
            assertEquals(1, newState.actions?.size)

            newState.actions?.forEach {
                assertEquals(true, it is ActionRemoveFavorite)
            }
        }
    }

    @After
    fun tearDown() {
        // Clean up the mock session after each test
        SessionManager.currentSession = null
    }

    companion object {
        @JvmField
        @ClassRule
        val mvrxTestRule = MavericksTestRule()
    }
}
