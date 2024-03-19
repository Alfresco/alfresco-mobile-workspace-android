package com.alfresco.content.browse.tasks.detail

import com.alfresco.content.actions.Action
import com.alfresco.content.browse.tasks.list.UpdateTasksData
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.content.process.ui.UpdateProcessData
import com.alfresco.events.EventBus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

/**
 * update the task list after complete the task
 */
internal fun TaskDetailViewModel.updateTaskList() {
    viewModelScope.launch {
        EventBus.default.send(UpdateTasksData(isRefresh = true))
        EventBus.default.send(UpdateProcessData(isRefresh = true))
    }
}

/**
 * execute "open with" action to download the content data
 */
fun TaskDetailViewModel.executePreview(action: Action) {
    val entry = action.entry as Entry
    val file = File(repository.session.contentDir, entry.fileName)
    if (!entry.isDocFile && repository.session.isFileExists(file) && file.length() != 0L) {
        entryListener?.onEntryCreated(Entry.updateDownloadEntry(entry, file.path))
    } else action.execute(context, GlobalScope)
}

/**
 * returns true if task completed otherwise false
 */
internal fun TaskDetailViewModel.isTaskCompleted(state: TaskDetailViewState): Boolean = state.parent?.endDate != null

internal fun TaskDetailViewModel.hasTaskStatusEnabled(state: TaskDetailViewState): Boolean = state.parent?.statusOption?.isNotEmpty() == true

/**
 * returns true if the endDate is empty and the assignee user is same as loggedIn user otherwise false
 */
fun TaskDetailViewModel.isCompleteButtonVisible(state: TaskDetailViewState): Boolean {
    if (isWorkflowTask) {
        return false
    }
    if (isTaskCompleted(state)) {
        return false
    }
    if (hasTaskEditMode) {
        return true
    }
    return state.parent?.assignee?.id == repository.getAPSUser().id
}

/**
 * returns true if taskFormStatus has value otherwise false
 */
fun TaskDetailViewModel.hasTaskStatusValue(state: TaskDetailViewState) = state.parent?.taskFormStatus != state.parent?.statusOption?.find { option -> option.id == "empty" }?.name

/**
 * return true if uploading files are in queue otherwise false
 */
internal fun TaskDetailViewModel.isFilesInQueue(state: TaskDetailViewState) = state.listContents.any { it.isUpload }

/**
 * removing the task related entries from local database
 */
internal fun TaskDetailViewModel.removeTaskEntries(state: TaskDetailViewState) {
    OfflineRepository().removeTaskEntries(state.parent?.id)
}

internal fun TaskDetailViewModel.isAssigneeAndLoggedInSame(assignee: UserGroupDetails?) = getAPSUser().id == assignee?.id
internal fun TaskDetailViewModel.isStartedByAndLoggedInSame(initiatorId: String?) = getAPSUser().id.toString() == initiatorId
internal fun TaskDetailViewModel.isTaskFormAndDetailRequestCompleted(state: TaskDetailViewState) = isWorkflowTask && state.requestTaskForm.complete
internal fun TaskDetailViewModel.isTaskDetailRequestCompleted(state: TaskDetailViewState) = !isWorkflowTask && state.request.complete
