package com.alfresco.content.listview

import com.alfresco.content.data.Entry

/**
 * Mark as NavigateFolderData
 */
interface FolderCreatedListener {

    /**
     * It will get called once folder created and browse to the same created folder.
     */
    fun onFolderCreated(entry: Entry)
}
