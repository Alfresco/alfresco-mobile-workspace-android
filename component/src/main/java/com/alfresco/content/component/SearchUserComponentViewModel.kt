package com.alfresco.content.component

import android.content.Context
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.ResponseUserList
import com.alfresco.content.data.TaskRepository
import com.alfresco.coroutines.asFlow
import kotlinx.coroutines.launch

/**
 * Mark as SearchUserComponentState class
 */
data class SearchUserComponentState(
    val parent: ComponentData?,
    val requestUser: Async<ResponseUserList> = Uninitialized
) : MavericksState

/**
 * Mark as SearchUserComponentViewModel class
 */
class SearchUserComponentViewModel(
    val context: Context,
    stateChipCreate: SearchUserComponentState,
    private val repository: TaskRepository
) : MavericksViewModel<SearchUserComponentState>(stateChipCreate) {

    fun getUserByName(name: String) = fetchUser(name = name)

    fun getUserByEmail(email: String) = fetchUser(email = email)

    private fun fetchUser(name: String = "", email: String = "") {
        viewModelScope.launch {
            // Fetch process user
            repository::searchUser.asFlow(name, email).execute {
                when (it) {
                    is Loading -> copy(requestUser = Loading())
                    is Fail -> copy(requestUser = Fail(it.error))
                    is Success -> {
                        copy(requestUser = Success(it()))
                    }
                    else -> {
                        this
                    }
                }
            }
        }
    }

    companion object : MavericksViewModelFactory<SearchUserComponentViewModel, SearchUserComponentState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: SearchUserComponentState
        ) = SearchUserComponentViewModel(viewModelContext.activity(), state, TaskRepository())
    }
}
