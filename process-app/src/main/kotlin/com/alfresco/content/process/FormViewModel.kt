package com.alfresco.content.process

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
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.ResponseListForm
import com.alfresco.content.data.TaskRepository
import com.alfresco.coroutines.asFlow
import kotlinx.coroutines.launch

data class FormViewState(
    val parent: ProcessEntry,
    val requestStartForm: Async<ResponseListForm> = Uninitialized,
) : MavericksState {
    constructor(target: ProcessEntry) : this(parent = target)
}

class FormViewModel(
    state: FormViewState,
    val context: Context,
    private val repository: TaskRepository,
) : MavericksViewModel<FormViewState>(state) {

    init {
        getStartForm(state.parent)
    }

    private fun getStartForm(processEntry: ProcessEntry) {
        requireNotNull(processEntry.id)
        viewModelScope.launch {
            repository::startForm.asFlow(processEntry.id).execute {
                when (it) {
                    is Loading -> copy(requestStartForm = Loading())
                    is Fail -> copy(requestStartForm = Fail(it.error))
                    is Success -> {
                        println("form-data == ${it()}")
                        copy(requestStartForm = Success(it()))
                    }

                    else -> {
                        this
                    }
                }
            }
        }
    }

    companion object : MavericksViewModelFactory<FormViewModel, FormViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: FormViewState,
        ) = FormViewModel(state, viewModelContext.activity(), TaskRepository())
    }
}
