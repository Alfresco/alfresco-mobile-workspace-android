package com.alfresco.content.listview

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

object MultiSelection {

    val multiSelectionChangedFlow = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)

    fun observeMultiSelection(): Flow<Boolean> = multiSelectionChangedFlow
}
