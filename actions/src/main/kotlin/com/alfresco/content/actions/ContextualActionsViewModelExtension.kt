package com.alfresco.content.actions

import com.alfresco.content.data.Entry
import com.alfresco.content.network.ConnectivityTracker

internal fun ContextualActionsViewModel.canPerformActionOverNetwork() = ConnectivityTracker.isActiveNetwork(context)
fun isMoveDeleteAllowed(entries: List<Entry>) = entries.isNotEmpty() && (entries.any { it.canDelete } && (entries.all { it.isFile || it.isFolder }))
