package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import com.alfresco.content.data.ParentEntry
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Marked as ActionUpdateFileFolder
 */
data class ActionUpdateFileFolder(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_rename,
    override val title: Int = R.string.action_rename_file_folder,
    override val eventName: EventName = EventName.RenameNode
) : Action {

    override suspend fun execute(context: Context): Entry {
        val result = showCreateFolderDialog(context) ?: throw CancellationException("User Cancellation")
        val nodeType = when (entry.type) {
            Entry.Type.FOLDER -> "cm:folder"
            else -> "cm:content"
        }
        val name = if (entry.isFile) "${result.name}.${entry.name.nameAndExtension().second}" else result.name
        return BrowseRepository().updateFileFolder(name, result.description, entry.id, nodeType)
    }

    private suspend fun showCreateFolderDialog(context: Context) = withContext(Dispatchers.Main) {
        suspendCoroutine {
            val name = if (entry.isFile) entry.name.nameAndExtension().first else entry.name
            CreateFolderDialog.Builder(context, true, name)
                .onSuccess { title, description ->
                    it.resume(CreateFolderMetadata(title, description))
                }
                .onCancel { it.resume(null) }
                .show()
        }
    }

    override fun copy(_entry: ParentEntry): Action = copy(entry = _entry as Entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(view, anchorView, R.string.action_rename_file_folder_toast, entry.name)

    private data class CreateFolderMetadata(
        val name: String,
        val description: String
    )

    private fun String.nameAndExtension(): Pair<String, String> {
        val file = File(this)
        return Pair(file.nameWithoutExtension, file.extension)
    }
}
