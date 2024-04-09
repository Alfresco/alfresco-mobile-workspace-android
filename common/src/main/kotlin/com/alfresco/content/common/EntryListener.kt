package com.alfresco.content.common

import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.payloads.FieldsData

/**
 * Mark as EntryListener interface
 */
interface EntryListener {

    /**
     * It will get called once new entry created.
     */
    fun onEntryCreated(entry: ParentEntry) {}

    /**
     * It will get called on tap of start workflow on the option list
     */
    fun onProcessStart(entries: List<ParentEntry>) {}

    fun onAttachFolder(entry: ParentEntry) {}
    fun onAttachFiles(field: FieldsData) {}
}
