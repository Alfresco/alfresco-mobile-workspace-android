package com.alfresco.content.browse.tasks.detail

import android.content.Context
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.payloads.CommentPayload
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

    var path: TaskPath = TaskPath.TASK_DETAILS

    init {
        getTaskDetails()
        getComments()
        getContents()
    }

    private fun getTaskDetails() = withState { state ->
        viewModelScope.launch {
            // Fetch tasks detail data
            repository::getTaskDetails.asFlow(
                state.taskDetailObj?.id ?: ""
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

    /**
     * gets all the comments from server by using give task Id.
     */
    fun getComments() = withState { state ->
        viewModelScope.launch {
            // Fetch tasks detail data
            repository::getComments.asFlow(
                state.taskDetailObj?.id ?: ""
            ).execute {
                when (it) {
                    is Loading -> copy(requestComments = Loading())
                    is Fail -> copy(requestComments = Fail(it.error))
                    is Success -> {
                        update(it()).copy(requestComments = Success(it()))
                    }
                    else -> {
                        this
                    }
                }
            }
        }
    }

    private fun getContents() = withState { state ->
        viewModelScope.launch {
            // Fetch tasks detail data
            repository::getContents.asFlow(
                state.taskDetailObj?.id ?: ""
            ).execute {
                when (it) {
                    is Loading -> copy(requestContents = Loading())
                    is Fail -> copy(requestContents = Fail(it.error))
                    is Success -> {
                        update(it()).copy(requestContents = Success(it()))
                    }
                    else -> {
                        this
                    }
                }
            }
        }
    }

    /**
     * execute the add comment api
     */
    fun addComment(message: String) = withState { state ->
        viewModelScope.launch {
            // Fetch tasks detail data
            repository::addComments.asFlow(
                state.taskDetailObj?.id ?: "", CommentPayload.with(message)
            ).execute {
                when (it) {
                    is Loading -> copy(requestAddComment = Loading())
                    is Fail -> copy(requestAddComment = Fail(it.error))
                    is Success -> {
                        getComments()
                        copy(requestAddComment = Success(it()))
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
