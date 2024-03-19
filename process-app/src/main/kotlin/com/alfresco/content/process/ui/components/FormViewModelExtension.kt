package com.alfresco.content.process.ui.components

import com.alfresco.content.process.FormViewModel
import com.alfresco.content.process.ui.UpdateProcessData
import com.alfresco.events.EventBus
import kotlinx.coroutines.launch

/**
 * update the list of workflow if new entry created
 */
fun FormViewModel.updateProcessList() {
    viewModelScope.launch {
        EventBus.default.send(UpdateProcessData(isRefresh = true))
    }
}
