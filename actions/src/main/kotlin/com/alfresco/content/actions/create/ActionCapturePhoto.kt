package com.alfresco.content.actions.create

import android.Manifest
import android.content.Context
import android.content.Intent
import android.view.View
import com.alfresco.capture.CaptureActivity
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
        if (PermissionFragment.requestPermission(
                context,
                Manifest.permission.CAMERA
            )) {
            context.startActivity(
                Intent(context, CaptureActivity::class.java).apply {
                    putExtras(CaptureArgs.makeArguments(entry.id))
                }
            )
        } else {
            throw Action.Exception(context.resources.getString(R.string.action_capture_failed_permissions))
        }

        return entry
    }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) = Unit
}
