package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ActionUpdateFileFolder(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_action_create_folder,
    override val title: Int = R.string.action_rename_file_folder
) : Action {

    override suspend fun execute(context: Context): Entry {
        val result = showCreateFolderDialog(context) ?: throw CancellationException("User Cancellation")
        val nodeType = when (entry.type) {
            Entry.Type.FOLDER -> "cm:folder"
            else -> "cm:content"
        }
        return BrowseRepository().updateFileFolder(result.name, result.description, entry.id, nodeType)
    }

    private suspend fun showCreateFolderDialog(context: Context) = withContext(Dispatchers.Main) {
        suspendCoroutine<CreateFolderMetadata?> {
            CreateFolderDialog.Builder(context, true)
                .onSuccess { title, description ->
                    it.resume(CreateFolderMetadata(title, description))
                }
                .onCancel { it.resume(null) }
                .show()
        }
    }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(view, anchorView, R.string.action_rename_file_folder_toast, entry.name)

    private data class CreateFolderMetadata(
        val name: String,
        val description: String
    )
}
