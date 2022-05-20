package com.alfresco.content.actions

import android.content.Context
import android.view.View
import com.alfresco.content.PermissionFragment
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.alfresco.scan.ScanHelperFragment
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Marked as ActionScanDocument data class
 */
data class ActionScanDocument(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_action_capture_photo,
    override val title: Int = R.string.action_scan_title
) : Action {

    private val repository = OfflineRepository()

    override suspend fun execute(context: Context): Entry {
        if (PermissionFragment.requestPermissions(
                context,
                ScanHelperFragment.requiredPermissions(),
                ScanHelperFragment.permissionRationale(context)
            )
        ) {
            val result = ScanHelperFragment.scanDocuments(context)
            if (!result.isNullOrEmpty()) {
                withContext(Dispatchers.IO) {
                    result.map { item ->
                        repository.scheduleForUpload(
                            item.uri.toString(),
                            entry.id,
                            item.filename,
                            item.description,
                            item.mimeType
                        )
                    }
                    repository.setTotalTransferSize(result.size)
                }
            } else {
                throw CancellationException("User Cancellation")
            }
        } else {
            throw Action.Exception(
                context.resources.getString(R.string.action_capture_failed_permissions)
            )
        }

        return entry
    }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(view, anchorView, R.string.action_upload_media_toast)
}