package com.alfresco.content.listview

import com.alfresco.content.data.Entry

/**
 * Mark as NavigateFolderData
 */
interface FolderCreatedListener {

    /**
     * It will get called once folder created and responsible to move user to the created screen.
     */
    fun onFolderCreated(entry: Entry)
}
