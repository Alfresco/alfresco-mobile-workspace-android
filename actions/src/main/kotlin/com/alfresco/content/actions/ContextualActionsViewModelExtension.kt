package com.alfresco.content.actions

import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineStatus

fun getFilteredEntries(entries: List<Entry>): List<Entry> {
    val filteredEntries = entries.filter {
        (!it.isUpload || it.offlineStatus == OfflineStatus.UNDEFINED) &&
            (it.offlineStatus == OfflineStatus.UNDEFINED || it.offlineStatus == OfflineStatus.SYNCED)
    }
    return filteredEntries
}

fun isMoveDeleteAllowed(entries: List<Entry>) = (entries.any { it.canDelete } && (entries.all { it.isFile || it.isFolder }))
