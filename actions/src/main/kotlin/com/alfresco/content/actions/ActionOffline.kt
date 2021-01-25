package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.alfresco.kotlin.ellipsize

data class ActionAddOffline(
    override val entry: Entry,
    override val icon: Int = R.drawable.ic_action_offline,
    override val title: Int = R.string.action_add_offline_title
) : Action {
    private val repository: OfflineRepository = OfflineRepository()

    override suspend fun execute(context: Context) =
        repository.markOffline(entry)

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(
            view,
            anchorView,
            R.string.action_add_offline_toast,
            entry.title.ellipsize(maxFileNameInToast(view))
        )
}

data class ActionRemoveOffline(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_action_offline_filled,
    override val title: Int = R.string.action_remove_offline_title
) : Action {
    private val repository: OfflineRepository = OfflineRepository()

    override suspend fun execute(context: Context) =
        repository.markForRemoval(entry)

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(
            view,
            anchorView,
            R.string.action_remove_offline_toast,
            entry.title.ellipsize(maxFileNameInToast(view))
        )
}
