package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.SitesRepository
import com.alfresco.content.data.TrashCanRepository
import com.alfresco.kotlin.ellipsize
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class ActionDelete(
    override var entry: Entry,
    override val entries: List<Entry> = emptyList(),
    override val icon: Int = R.drawable.ic_delete,
    override val title: Int = R.string.action_delete_title,
    override val eventName: EventName = EventName.MoveTrash,
) : Action {

    override suspend fun execute(context: Context): Entry {
        try {
            withContext(Dispatchers.IO) {
                if (entries.isNotEmpty()) {
                    for (entryObj in entries) {
                        println("data entry id == ${entryObj.id}")
                        delete(entryObj)
                    }
                } else {
                    delete(entry)
                }
            }
        } catch (ex: KotlinNullPointerException) {
            // no-op. expected for 204
            ex.printStackTrace()
        }

        // Cleanup associated upload if any
        if (entries.isNotEmpty()) {
            entries.map {
                if (it.type == Entry.Type.FILE) {
                    OfflineRepository().removeUpload(it.id)
                }
            }
        } else {
            if (entry.type == Entry.Type.FILE) {
                OfflineRepository().removeUpload(entry.id)
            }
        }

        return entry
    }

    private suspend inline fun delete(entry: Entry) {
        when (entry.type) {
            Entry.Type.FILE, Entry.Type.FOLDER -> BrowseRepository().deleteEntry(entry)
            Entry.Type.SITE -> SitesRepository().deleteSite(entry)
            else -> {}
        }
    }

    override fun copy(_entry: ParentEntry): Action = copy(entry = _entry as Entry)

    override fun showToast(view: View, anchorView: View?) {
        if (entries.size > 1) {
            Action.showToast(view, anchorView, R.string.action_delete_multiple_toast, entries.size.toString())
        } else {
            Action.showToast(view, anchorView, R.string.action_delete_toast, entry.name.ellipsize(maxFileNameInToast(view)))
        }
    }
}

data class ActionRestore(
    override var entry: Entry,
    override val entries: List<Entry> = emptyList(),
    override val icon: Int = R.drawable.ic_restore,
    override val title: Int = R.string.action_restore_title,
    override val eventName: EventName = EventName.Restore,
) : Action {
    override suspend fun execute(context: Context): Entry {
        withContext(Dispatchers.IO) {
            if (entries.isNotEmpty()) {
                entries.forEach { entryObj ->
                    TrashCanRepository().restoreEntry(entryObj)
                }
            } else {
                TrashCanRepository().restoreEntry(entry)
            }
        }
        return entry
    }

    override fun copy(_entry: ParentEntry): Action = copy(entry = _entry as Entry)

    override fun showToast(view: View, anchorView: View?) {
        if (entries.size > 1) {
            Action.showToast(view, anchorView, R.string.action_restored_multiple_toast, entries.size.toString())
        } else {
            Action.showToast(view, anchorView, R.string.action_restored_toast, entry.name.ellipsize(maxFileNameInToast(view)))
        }
    }
}

data class ActionDeleteForever(
    override var entry: Entry,
    override val entries: List<Entry> = emptyList(),
    override val icon: Int = R.drawable.ic_delete_forever,
    override val title: Int = R.string.action_delete_forever_title,
    override val eventName: EventName = EventName.PermanentlyDelete,
) : Action {

    override suspend fun execute(context: Context): Entry {
        if (showConfirmation(context)) {
            try {
                withContext(Dispatchers.IO) {
                    if (entries.isNotEmpty()) {
                        entries.forEach { entryObj ->
                            delete(entryObj)
                        }
                    } else {
                        delete(entry)
                    }
                }
            } catch (ex: KotlinNullPointerException) {
                // no-op. expected for 204
                ex.printStackTrace()
            }
        } else {
            throw CancellationException()
        }

        return entry
    }

    private suspend fun showConfirmation(context: Context) = withContext(Dispatchers.Main) {
        suspendCoroutine<Boolean> {
            MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.action_delete_confirmation_title))
                .setMessage(
                    if (entries.size > 1) {
                        context.getString(
                            R.string.action_delete_multiple_confirmation_message,
                            entries.size.toString(),
                        )
                    } else context.getString(R.string.action_delete_confirmation_message, entry.name),
                )
                .setNegativeButton(context.getString(R.string.action_delete_confirmation_negative)) { _, _ ->
                    it.resume(false)
                }
                .setPositiveButton(context.getString(R.string.action_delete_confirmation_positive)) { _, _ ->
                    it.resume(true)
                }
                .show()
        }
    }

    private suspend inline fun delete(entry: Entry) = TrashCanRepository().deleteForeverEntry(entry)

    override fun copy(_entry: ParentEntry): Action = copy(entry = _entry as Entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(
            view,
            anchorView,
            R.string.action_delete_forever_toast,
            entry.name.ellipsize(maxFileNameInToast(view)),
        )
}
