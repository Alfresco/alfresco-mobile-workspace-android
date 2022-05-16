package com.alfresco.content.listview

import com.alfresco.content.data.Entry

/**
 * Mark as NavigateFolderData
 */
interface FolderCreatedListener {
    fun onFolderCreated(entry: Entry)
}
