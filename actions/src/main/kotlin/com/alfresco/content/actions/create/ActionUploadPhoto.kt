package com.alfresco.content.actions.create

import android.content.Context
import android.view.View
import com.alfresco.content.actions.Action
import com.alfresco.content.actions.R
import com.alfresco.content.data.Entry

data class ActionUploadPhoto(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_action_upload_photo,
    override val title: Int = R.string.action_upload_photo_title
) : Action {
    override suspend fun execute(context: Context): Entry {
        TODO("Not yet implemented")
    }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) = Unit
}
