package com.alfresco.content.actions

import android.Manifest
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.alfresco.content.PermissionFragment
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import java.io.File
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
            if (entry.isSynced) {
                exportFile(context)
            } else {
                enqueueDownload(context)
            }
        } else {
            throw Action.Exception(context.resources.getString(R.string.action_download_failed_permissions))
        }

        return entry
    }

    private fun exportFile(context: Context) {
        val src = OfflineRepository().contentFile(entry)
        val filename = entry.title

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val mimeType = DocumentFile.fromFile(src).type

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            }
            resolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )?.also {
                resolver.openOutputStream(it).use { output ->
                    requireNotNull(output)

                    src.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
            }
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            requireNotNull(dir)

            val target = File(dir, filename)
            src.copyTo(target, true)
        }
    }

    private fun enqueueDownload(context: Context) {
        val uri = BrowseRepository().contentUri(entry)

        val dm = ContextCompat.getSystemService(context, DownloadManager::class.java)
        val request = DownloadManager.Request(Uri.parse(uri))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle(entry.title)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, entry.title)

        dm?.enqueue(request) ?: throw CancellationException("Missing DownloadManager service.")
    }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) =
        toastMessage.let { Action.showToast(view, anchorView, it) }

    private val toastMessage = if (entry.isSynced) {
        R.string.action_export_toast
    } else {
        R.string.action_download_toast
    }
}
