package com.alfresco.content.actions.create

import android.Manifest
import android.content.Context
import android.view.View
import com.alfresco.capture.CaptureHelperFragment
import com.alfresco.content.PermissionFragment
import com.alfresco.content.actions.Action
import com.alfresco.content.actions.R
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository

data class ActionCapturePhoto(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_action_capture_photo,
    override val title: Int = R.string.action_capture_photo_title
) : Action {

    private val repository = OfflineRepository()

    override suspend fun execute(context: Context): Entry {
        if (PermissionFragment.requestPermission(
                context,
                Manifest.permission.CAMERA
            )) {
            val item = CaptureHelperFragment.capturePhoto(context)
            if (item != null) {
                repository.scheduleForUpload(
                    item.uri.toString(),
                    entry.id,
                    item.name,
                    item.description,
                    item.mimeType
                )
            }
        } else {
            throw Action.Exception(context.resources.getString(R.string.action_capture_failed_permissions))
        }

        return entry
    }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) = Unit
}
