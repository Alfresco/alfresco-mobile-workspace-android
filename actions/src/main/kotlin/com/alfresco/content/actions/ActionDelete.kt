package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.SitesRepository
import com.alfresco.content.data.TrashCanRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ActionDelete(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_delete,
    override val title: Int = R.string.action_delete_title
) : Action {

    override suspend fun execute(context: Context): Entry {
        try {
            delete(entry)
        } catch (ex: KotlinNullPointerException) {
            // no-op. expected for 204
        }

        return entry
    }
    private suspend inline fun delete(entry: Entry) {
        when (entry.type) {
            Entry.Type.File, Entry.Type.Folder -> BrowseRepository().deleteEntry(entry)
            Entry.Type.Site -> SitesRepository().deleteSite(entry)
            else -> { }
        }
    }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(view, anchorView, R.string.action_delete_toast, entry.title)
}

data class ActionRestore(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_restore,
    override val title: Int = R.string.action_restore_title
) : Action {
    override suspend fun execute(context: Context): Entry {
        TrashCanRepository().restoreEntry(entry)
        return entry
    }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(view, anchorView, R.string.action_restored_toast, entry.title)
}

data class ActionDeleteForever(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_delete_forever,
    override val title: Int = R.string.action_delete_forever_title
) : Action {
    override suspend fun execute(context: Context): Entry {
        if (showConfirmation(context)) {
            try {
                delete(entry)
            } catch (ex: KotlinNullPointerException) {
                // no-op. expected for 204
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
                .setMessage(context.getString(R.string.action_delete_confirmation_message, entry.title))
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

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(view, anchorView, R.string.action_delete_forever_toast, entry.title)
}