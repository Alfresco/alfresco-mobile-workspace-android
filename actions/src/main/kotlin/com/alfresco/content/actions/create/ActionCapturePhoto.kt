package com.alfresco.content.actions.create

import android.Manifest
import android.content.Context
import android.content.Intent
import android.view.View
import com.alfresco.capture.CameraActivity
import com.alfresco.capture.CaptureArgs
import com.alfresco.content.PermissionFragment
import com.alfresco.content.actions.Action
import com.alfresco.content.actions.R
import com.alfresco.content.data.Entry

data class ActionCapturePhoto(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_action_capture_photo,
    override val title: Int = R.string.action_capture_photo_title
) : Action {

    override suspend fun execute(context: Context): Entry {
        try {
            PermissionFragment.requestPermissions(
                context,
                Manifest.permission.CAMERA
            )
        } catch (_: Exception) {
        }

        context.startActivity(
            Intent(context, CameraActivity::class.java).apply {
                putExtras(CaptureArgs.bundle(entry.id))
            }
        )

        return entry
    }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) = Unit
}
