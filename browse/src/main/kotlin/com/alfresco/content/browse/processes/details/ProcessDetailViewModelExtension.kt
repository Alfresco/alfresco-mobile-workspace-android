package com.alfresco.content.browse.processes.details

import com.alfresco.content.browse.processes.list.UpdateProcessData
import com.alfresco.content.browse.tasks.list.UpdateTasksData
import com.alfresco.events.EventBus
import kotlinx.coroutines.launch

/**
 * update the list of workflow if new entry created
 */
fun ProcessDetailViewModel.updateProcessList() {
    viewModelScope.launch {
        EventBus.default.send(UpdateProcessData(isRefresh = true))
        EventBus.default.send(UpdateTasksData(isRefresh = true))
    }
}
