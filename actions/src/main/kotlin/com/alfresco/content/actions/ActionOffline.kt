package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.OfflineStatus
import com.alfresco.content.data.ParentEntry
import com.alfresco.kotlin.ellipsize

data class ActionAddOffline(
    override val entry: Entry,
    override val entries: List<Entry> = emptyList(),
    override val icon: Int = R.drawable.ic_action_offline,
    override val title: Int = R.string.action_add_offline_title,
    override val eventName: EventName = EventName.MarkOffline,
) : Action {
    private val repository: OfflineRepository = OfflineRepository()

    override suspend fun execute(context: Context): Entry {
        val res = repository.markForSync(entry)
        // return item without status
        return res.copy(offlineStatus = OfflineStatus.UNDEFINED)
    }

    override suspend fun executeMulti(context: Context): Pair<Entry, List<Entry>> {
        val entriesObj = entries.toMutableList()
        entries.forEachIndexed { index, obj ->
            val res = repository.markForSync(obj)
            // return item without status
            entriesObj[index] = res.copy(offlineStatus = OfflineStatus.UNDEFINED)
        }
        return Pair(entry, entriesObj)
    }

    override fun copy(_entry: ParentEntry): Action = copy(entry = _entry as Entry)

    override fun copy(_entries: List<Entry>): Action = copy(entries = _entries)

    override fun showToast(view: View, anchorView: View?) {
        if (entries.size > 1) {
            Action.showToast(view, anchorView, R.string.action_add_offline_multiple_toast, entries.size.toString())
        } else {
            Action.showToast(view, anchorView, R.string.action_add_offline_toast, entry.name.ellipsize(maxFileNameInToast(view)))
        }
    }
}

data class ActionRemoveOffline(
    override var entry: Entry,
    override val entries: List<Entry> = emptyList(),
    override val icon: Int = R.drawable.ic_action_offline_filled,
    override val title: Int = R.string.action_remove_offline_title,
    override val eventName: EventName = EventName.RemoveOffline,
) : Action {
    private val repository: OfflineRepository = OfflineRepository()

    override suspend fun execute(context: Context): ParentEntry {
        return repository.removeFromSync(entry)
    }

    override suspend fun executeMulti(context: Context): Pair<Entry, List<Entry>> {
        val entriesObj = entries.toMutableList()
        entries.forEachIndexed { index, obj ->
            entriesObj[index] = repository.removeFromSync(obj)
        }
        return Pair(entry, entriesObj)
    }

    override fun copy(_entry: ParentEntry): Action = copy(entry = _entry as Entry)
    override fun copy(_entries: List<Entry>): Action = copy(entries = _entries)
    override fun showToast(view: View, anchorView: View?) {
        if (entries.size > 1) {
            Action.showToast(view, anchorView, R.string.action_remove_offline_multiple_toast, entries.size.toString())
        } else {
            Action.showToast(view, anchorView, R.string.action_remove_offline_toast, entry.name.ellipsize(maxFileNameInToast(view)))
        }
    }
}

// Not a typical action - used as an event.
data class ActionSyncNow(val overrideNetwork: Boolean)
