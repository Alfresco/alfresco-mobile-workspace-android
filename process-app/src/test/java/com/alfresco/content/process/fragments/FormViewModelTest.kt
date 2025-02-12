package com.alfresco.content.process.fragments

import android.content.Context
import com.airbnb.mvrx.test.MavericksTestRule
import com.alfresco.content.data.DefaultOutcomesID
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.ui.fragments.FormViewModel
import com.alfresco.content.process.ui.fragments.FormViewState
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.coroutines.CoroutineContext

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

    private val mockSession: Session = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {

        // Mock SessionManager to return our mock session
        mockkObject(SessionManager)
        every { SessionManager.requireSession } returns mockSession

        // Override Dispatchers.Main for testing coroutines
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `test getAPSUser returns user`() = runTest {

        val initialState = FormViewState()
        val viewModel = spyk(FormViewModel(initialState, context, repository, null))

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

        val initialState = FormViewState()

        val viewModel = FormViewModel(initialState, context, repository, null)
        advanceUntilIdle()

        viewModel.updateStateData(false, listOf(FieldsData(id = "field1", value = "newValue")))

        viewModel.updateFieldValue(fieldId, newValue, errorData)

        val newState = viewModel.awaitState()

        var updatedField: FieldsData? = newState.formFields.find { it.id == fieldId }

        assertEquals(newValue, updatedField?.value)
    }

    // Add test cases for hasFieldValidData

    @Test
    fun `test hasFieldValidData with valid fields`() = runTest{
        // Arrange: Mock some valid fields data
        val validField1 = FieldsData(
            required = true,
            value = "Valid Value",
            errorData = Pair(false, ""),
            options = emptyList() // No dropdown options
        )
        val validField2 = FieldsData(
            required = false,
            value = "Valid Value",
            errorData = Pair(false, ""),
            options = emptyList() // No dropdown options
        )
        val validField3 = FieldsData(
            required = true,
            value = "Option 1",
            errorData = Pair(false, ""),
            options = listOf(OptionsModel(id = "12","Option 1", "validId"))
        )

        val viewModel = FormViewModel(FormViewState(), context, repository, null)


        val fields = listOf(validField1, validField2, validField3)

        // Act: Call the method under test
        val result = viewModel.hasFieldValidData(fields)

        // Assert: The result should be true as all fields are valid
        assertTrue("Expected true, but got $result", result)
    }

    /* @Test
     fun `test hasFieldValidData with required field missing data`() {
         // Arrange: Mock some fields data where a required field has no value
         val invalidField1 = FieldsData(
             required = true,
             value = null,
             errorData = Pair(false, ""),
             options = emptyList() // No dropdown options
         )
         val validField2 = FieldsData(
             required = false,
             value = "Valid Value",
             errorData = Pair(false, ""),
             options = emptyList() // No dropdown options
         )
         val validField3 = FieldsData(
             required = true,
             value = "Option 1",
             errorData = Pair(false, ""),
             options = listOf(Option("Option 1", "validId"))
         )

         val fields = listOf(invalidField1, validField2, validField3)

         // Act: Call the method under test
         val result = viewModel.hasFieldValidData(fields)

         // Assert: The result should be false due to the invalid required field
         assertFalse(result)
     }

     @Test
     fun `test hasFieldValidData with error in dropdown required field`() {
         // Arrange: Mock a scenario where a required dropdown field has invalid selection
         val invalidDropdownField = FieldsData(
             required = true,
             value = "Invalid Option",
             errorData = Pair(false, ""),
             options = listOf(Option("Option 1", "validId"), Option("Option 2", "empty"))
         )
         val validField = FieldsData(
             required = false,
             value = "Valid Value",
             errorData = Pair(false, ""),
             options = emptyList() // No dropdown options
         )

         val fields = listOf(invalidDropdownField, validField)

         // Act: Call the method under test
         val result = viewModel.hasFieldValidData(fields)

         // Assert: The result should be false due to invalid dropdown selection
         assertFalse(result)
     }

     @Test
     fun `test hasFieldValidData with error in non-required field`() {
         // Arrange: Mock a scenario where a non-required field has an error
         val invalidNonRequiredField = FieldsData(
             required = false,
             value = "Invalid Value",
             errorData = Pair(true, "Some error"),
             options = emptyList() // No dropdown options
         )

         val validField = FieldsData(
             required = true,
             value = "Valid Value",
             errorData = Pair(false, ""),
             options = emptyList() // No dropdown options
         )

         val fields = listOf(invalidNonRequiredField, validField)

         // Act: Call the method under test
         val result = viewModel.hasFieldValidData(fields)

         // Assert: The result should be false due to error in non-required field
         assertFalse(result)
     }

     // More test cases

     @Test
     fun `test hasFieldValidData with multiple required fields with errors`() {
         // Arrange: Mock some invalid fields data
         val invalidField1 = FieldsData(
             required = true,
             value = null,
             errorData = Pair(true, "Error in field1"),
             options = emptyList() // No dropdown options
         )
         val invalidField2 = FieldsData(
             required = true,
             value = "Invalid Option",
             errorData = Pair(true, "Error in field2"),
             options = listOf(Option("Option 1", "validId"), Option("Option 2", "empty"))
         )

         val fields = listOf(invalidField1, invalidField2)

         // Act: Call the method under test
         val result = viewModel.hasFieldValidData(fields)

         // Assert: The result should be false due to errors in required fields
         assertFalse(result)
     }*/
}
