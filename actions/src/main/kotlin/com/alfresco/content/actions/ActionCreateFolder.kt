package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ActionCreateFolder(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_action_create_folder,
    override val title: Int = R.string.action_create_folder,
    override val eventName: EventName = EventName.CreateFolder
) : Action {

    override suspend fun execute(context: Context): Entry {
        val result = showCreateFolderDialog(context) ?: throw CancellationException("User Cancellation")
        return BrowseRepository().createFolder(result.name, result.description, entry.id)
    }

    private suspend fun showCreateFolderDialog(context: Context) = withContext(Dispatchers.Main) {
        suspendCoroutine<CreateFolderMetadata?> {
            CreateFolderDialog.Builder(context, false, entry.name)
                .onSuccess { title, description ->
                    it.resume(CreateFolderMetadata(title, description))
                }
                .onCancel { it.resume(null) }
                .show()
        }
    }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(view, anchorView, R.string.action_create_folder_toast, entry.name)

    private data class CreateFolderMetadata(
        val name: String,
        val description: String
    )
}
