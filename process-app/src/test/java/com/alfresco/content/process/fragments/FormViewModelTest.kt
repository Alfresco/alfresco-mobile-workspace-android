package com.alfresco.content.process.fragments


import android.content.Context
import com.airbnb.mvrx.test.MavericksTestRule
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.content.process.ui.fragments.FormViewModel
import com.alfresco.content.process.ui.fragments.FormViewState
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FormViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @get:Rule
    val mvRxTestRule = MavericksTestRule()

    @MockK(relaxed = true)
    lateinit var repository: TaskRepository

    @MockK(relaxed = true)
    lateinit var context: Context

    private lateinit var viewModel: FormViewModel
    private lateinit var initialState: FormViewState

    private val mockSession: Session = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {

        // Mock SessionManager to return our mock session
        mockkObject(SessionManager)
        every { SessionManager.requireSession } returns mockSession

        // Override Dispatchers.Main for testing coroutines
        Dispatchers.setMain(testDispatcher)

        initialState = FormViewState()
        viewModel = spyk(FormViewModel(initialState, context, repository, null))
    }

    @Test
    fun `test getAPSUser returns user`() = runTest {
        val expectedUser = UserGroupDetails(id = 1, firstName = "Test", lastName = "User")
        coEvery { repository.getAPSUser() } returns expectedUser

        val user = viewModel.getAPSUser()
        assertEquals(expectedUser, user)
    }

    @Test
    fun `test updateFieldValue updates state`() = runTest {
        val fieldId = "field1"
        val newValue = "newValue"
        val errorData = Pair(false, "")

        viewModel.updateFieldValue(fieldId, newValue, errorData)

        val updatedField = viewModel.state.formFields.find { it.id == fieldId }
        assertEquals(newValue, updatedField?.value)
    }


    /*@Test
    fun `test performOutcomes calls correct method`() {
        val optionsModel = mockk<OptionsModel> {
            every { id } returns DefaultOutcomesID.DEFAULT_COMPLETE.value()
        }

        viewModel.performOutcomes(optionsModel)
        verify { viewModel.completeTask() }
    }*/
}
