package com.alfresco.content.browse.tasks.detail

import com.alfresco.content.actions.Action
import com.alfresco.content.browse.tasks.list.UpdateTasksData
import com.alfresco.content.data.Entry
import com.alfresco.events.EventBus
import java.io.File
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * update the task list after complete the task
 */
fun TaskDetailViewModel.updateTaskList() {
    viewModelScope.launch {
        EventBus.default.send(UpdateTasksData(isRefresh = true))
    }
}

/**
 * execute "open with" action to download the content data
 */
fun TaskDetailViewModel.execute(action: Action) {
    val file = File(repository.session.contentDir, action.entry.name)
    if (repository.session.isFileExists(file)) {
        entryListener?.onEntryCreated(Entry.updateDownloadEntry(action.entry, file.path))
    } else action.execute(context, GlobalScope)
}

/**
 * returns true if task completed otherwise false
 */
fun TaskDetailViewModel.isTaskCompleted(state: TaskDetailViewState): Boolean = state.parent?.endDate != null

/**
 * returns true if the endDate is empty and the assignee user is same as loggedIn user otherwise false
 */
fun TaskDetailViewModel.isCompleteButtonVisible(state: TaskDetailViewState): Boolean =
    !isTaskCompleted(state) && state.parent?.assignee?.email == repository.userEmail
