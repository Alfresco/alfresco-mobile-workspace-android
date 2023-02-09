package com.alfresco.content.listview

import com.alfresco.content.data.ParentEntry

/**
 * Mark as EntryListener interface
 */
interface EntryListener {

    /**
     * It will get called once new entry created.
     */
    fun onEntryCreated(entry: ParentEntry) {}
}
