package com.alfresco.content.actions

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.View
import androidx.core.content.ContextCompat
import com.alfresco.content.PermissionFragment
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import kotlin.coroutines.cancellation.CancellationException

data class ActionDownload(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_download,
    override val title: Int = R.string.action_download_title
) : Action {

    override suspend fun execute(context: Context): Entry {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            PermissionFragment.requestPermissions(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )) {
            enqueueDownload(context)
        } else {
            throw Error(context.resources.getString(R.string.action_download_failed_permissions))
        }

        return entry
    }

    private fun enqueueDownload(context: Context) {
        val uri = BrowseRepository().contentUri(entry.id)

        val dm = ContextCompat.getSystemService(context, DownloadManager::class.java)
        val request = DownloadManager.Request(Uri.parse(uri))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle(entry.title)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, entry.title)

        dm?.enqueue(request) ?: throw CancellationException("Missing DownloadManager service.")
    }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) =
        Action.showToast(view, anchorView, R.string.action_download_toast)
}
