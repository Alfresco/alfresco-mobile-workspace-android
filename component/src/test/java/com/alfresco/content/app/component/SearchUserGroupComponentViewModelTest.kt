package com.alfresco.content.app.component

import android.content.Context
import com.airbnb.mvrx.test.MavericksTestRule
import com.airbnb.mvrx.withState
import com.alfresco.content.component.SearchUserGroupComponentState
import com.alfresco.content.component.SearchUserGroupComponentViewModel
import com.alfresco.content.component.SearchUserGroupParams
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.TaskRepository
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.ClassRule
import org.junit.Test

class SearchUserGroupComponentViewModelTest {

    private lateinit var viewModel: SearchUserGroupComponentViewModel

    private lateinit var state: SearchUserGroupComponentState

    private lateinit var entry: ParentEntry

    private val context: Context = mockk(relaxed = true)

    private val repository: TaskRepository = mockk(relaxed = true)

    @Test
    fun searchQuery_empty() {

        entry = ProcessEntry()

        state = SearchUserGroupComponentState(entry)

        viewModel = SearchUserGroupComponentViewModel(context, state, repository)

        viewModel.params = SearchUserGroupParams()

        assertEquals(viewModel.params.nameOrIndividual, "")
        assertEquals(viewModel.params.emailOrGroups, "")
    }

    @Test
    fun nameOrIndividual_notEmpty() {

        entry = ProcessEntry()

        state = SearchUserGroupComponentState(entry)

        viewModel = SearchUserGroupComponentViewModel(context, state, repository)

        viewModel.params = SearchUserGroupParams()

        viewModel.searchByNameOrIndividual = true
        viewModel.setSearchQuery("demo")

        withState(viewModel) { state ->
            assertNotEquals(viewModel.params.nameOrIndividual, "")
            assertEquals(viewModel.params.emailOrGroups, "")
        }
    }

    @Test
    fun emailOrGroup_notEmpty() {

        entry = ProcessEntry()

        state = SearchUserGroupComponentState(entry)

        viewModel = SearchUserGroupComponentViewModel(context, state, repository)

        viewModel.params = SearchUserGroupParams()

        viewModel.searchByNameOrIndividual = false
        viewModel.setSearchQuery("demo@alfresco.com")

        withState(viewModel) { state ->
            assertEquals(viewModel.params.nameOrIndividual, "")
            assertNotEquals(viewModel.params.emailOrGroups, "")
        }
    }

    companion object {
        @JvmField
        @ClassRule
        val mvrxTestRule = MavericksTestRule()
    }
}
