package com.alfresco.content.browse.tasks.detail

import android.content.Context
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.TaskRepository
import com.alfresco.coroutines.asFlow
import kotlinx.coroutines.launch

/**
 * Marked as TaskDetailViewModel class
 */
class TaskDetailViewModel(
    state: TaskDetailViewState,
    val context: Context,
    private val repository: TaskRepository
) : MavericksViewModel<TaskDetailViewState>(state) {

    init {
        getTaskDetails()
    }

    private fun getTaskDetails() = withState { state ->
        viewModelScope.launch {
            // Fetch tasks detail data
            repository::getTaskDetails.asFlow(
                state.taskID
            ).execute {
                when (it) {
                    is Loading -> copy(request = Loading())
                    is Fail -> copy(request = Fail(it.error))
                    is Success -> {
                        update(it()).copy(request = Success(it()))
                    }
                    else -> {
                        this
                    }
                }
            }
        }
    }

    companion object : MavericksViewModelFactory<TaskDetailViewModel, TaskDetailViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: TaskDetailViewState
        ) = TaskDetailViewModel(state, viewModelContext.activity, TaskRepository())
    }
}
