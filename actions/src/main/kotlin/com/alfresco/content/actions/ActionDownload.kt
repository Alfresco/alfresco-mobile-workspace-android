package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.Entry

data class ActionDownload(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_download,
    override val title: Int = R.string.action_download_title
) : Action {

    override suspend fun execute(context: Context): Entry {
        // TODO:
        return entry
    }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) {
        // no-op
    }
}
