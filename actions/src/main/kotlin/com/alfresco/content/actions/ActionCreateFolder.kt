package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.withFragment
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine

data class ActionCreateFolder(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_action_create_folder,
    override val title: Int = R.string.action_create_folder
) : Action {

    private var onResult: CancellableContinuation<CreateFolderDataModel?>? = null

    override suspend fun execute(context: Context): Entry {


        val createFolderDialog = CreateFolderDialog()
        createFolderDialog.onSuccess = { dataModel ->
            onResult?.resume(dataModel, null)
        }

        val result = withFragment(context, TAG, { openFolderDialog() }, { createFolderDialog })

        val newEntry: Entry
        if (result != null) {
            newEntry = entry.copy(name = result.name)
            BrowseRepository().createFolder(result.name, result.description, entry.id)
        } else {
            throw CancellationException("User Cancellation")
        }

        return newEntry
    }

    private suspend fun openFolderDialog(): CreateFolderDataModel? =
        suspendCancellableCoroutine { continuation ->
            onResult = continuation
        }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(view, anchorView, R.string.action_create_folder_toast, entry.name)

    companion object {
        private val TAG = CreateFolderDialog::class.java.simpleName

    }

}

