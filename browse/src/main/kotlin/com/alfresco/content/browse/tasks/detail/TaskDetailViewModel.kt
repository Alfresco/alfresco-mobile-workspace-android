package com.alfresco.content.browse.tasks.detail

import android.content.Context
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.actions.Action
import com.alfresco.content.actions.ActionOpenWith
import com.alfresco.content.actions.ActionUpdateNameDescription
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.payloads.CommentPayload
import com.alfresco.content.listview.EntryListener
import com.alfresco.coroutines.asFlow
import com.alfresco.events.on
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Marked as TaskDetailViewModel class
 */
class TaskDetailViewModel(
    state: TaskDetailViewState,
    val context: Context,
    val repository: TaskRepository
) : MavericksViewModel<TaskDetailViewState>(state) {

    var isAddComment = false
    var hasTaskEditMode = false
    var entryListener: EntryListener? = null

    init {
        getTaskDetails()
        getComments()
        getContents()
        viewModelScope.on<ActionOpenWith> {
            if (!it.entry.path.isNullOrEmpty())
                entryListener?.onEntryCreated(it.entry)
        }
        viewModelScope.on<ActionUpdateNameDescription> {
            setState { copy(parent = it.entry) }
        }
    }

    private fun getTaskDetails() = withState { state ->
        viewModelScope.launch {
            // Fetch tasks detail data
            repository::getTaskDetails.asFlow(
                state.parent?.id ?: ""
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
                state.parent?.id ?: ""
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
                state.parent?.id ?: ""
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
                state.parent?.id ?: "", CommentPayload.with(message)
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

    /**
     * execute the complete task api
     */
    fun completeTask() = withState { state ->
        viewModelScope.launch {
            // Fetch tasks detail data
            repository::completeTask.asFlow(
                state.parent?.id ?: ""
            ).execute {
                when (it) {
                    is Loading -> copy(requestCompleteTask = Loading())
                    is Fail -> copy(requestCompleteTask = Fail(it.error))
                    is Success -> {
                        copy(requestCompleteTask = Success(it()))
                    }
                    else -> {
                        this
                    }
                }
            }
        }
    }

    fun updateTaskDetails() = withState { state ->
        requireNotNull(state.parent)
        viewModelScope.launch {
            // Fetch tasks detail data
            repository::updateTaskDetails.asFlow(
                state.parent
            ).execute {
                when (it) {
                    is Loading -> copy(requestUpdateTask = Loading())
                    is Fail -> copy(requestUpdateTask = Fail(it.error))
                    is Success -> {
                        copy(requestUpdateTask = Success(it()))
                    }
                    else -> {
                        this
                    }
                }
            }
        }
    }

    /**
     * adding listener to update the View after downloading the content
     */
    fun setListener(listener: EntryListener) {
        this.entryListener = listener
    }

    /**
     * It will execute while showing the dialog to update task name and description.
     */
    fun execute(action: Action) = action.execute(context, GlobalScope)

    companion object : MavericksViewModelFactory<TaskDetailViewModel, TaskDetailViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: TaskDetailViewState
        ) = TaskDetailViewModel(state, viewModelContext.activity(), TaskRepository())
    }
}
