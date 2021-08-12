package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository

data class ActionCreateFolder(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_action_capture_photo,
    override val title: Int = R.string.action_create_folder
) : Action {

    private val repository = OfflineRepository()

    override suspend fun execute(context: Context): Entry {

        val result = CreateFolderFragment.openFolderDialog(context)

        result?.let {

            repository.createFolder(it.name, it.description,entry.id)

        }

        return entry
    }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(view, anchorView, R.string.action_upload_media_toast)
}
