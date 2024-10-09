package com.alfresco.content.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

object MultiSelection {
    val multiSelectionChangedFlow = MutableSharedFlow<MultiSelectionData>(extraBufferCapacity = 1)

    val clearSelectionChangedFlow = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)

    fun observeMultiSelection(): Flow<MultiSelectionData> = multiSelectionChangedFlow

    fun observeClearSelection(): Flow<Boolean> = clearSelectionChangedFlow
}

data class MultiSelectionData(
    val selectedEntries: List<Entry> = emptyList(),
    val isMultiSelectionEnabled: Boolean = false,
    val path: String = "",
)
