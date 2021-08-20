package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import kotlinx.coroutines.CancellationException

data class ActionCreateFolder(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_action_create_folder,
    override val title: Int = R.string.action_create_folder
) : Action {

    private val repository = OfflineRepository()

    override suspend fun execute(context: Context): Entry {

        val result = CreateFolderFragment.openFolderDialog(context)
        val newEntry: Entry
        if (result != null) {
            newEntry = entry.copy(name = result.name)
            repository.createFolder(result.name, result.description, entry.id)
        } else {
            throw CancellationException("User Cancellation")
        }

        return newEntry
    }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(view, anchorView, R.string.action_create_folder_toast, entry.name)
}
