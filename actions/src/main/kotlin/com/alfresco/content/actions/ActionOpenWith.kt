package com.alfresco.content.actions

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.mimetype.MimeType
import com.alfresco.download.ContentDownloader
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient

data class ActionOpenWith(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_open_with,
    override val title: Int = R.string.action_open_with_title,
    override val eventName: EventName = EventName.OpenWith,
    val hasChooser: Boolean = false
) : Action {

    private var deferredDownload = AtomicReference<Deferred<Unit>?>(null)

    override suspend fun execute(context: Context): Entry {
        val target = if (entry.isSynced) {
            OfflineRepository().contentFile(entry)
        } else {
            fetchRemoteFile(context)
        }

        return if (!entry.isProcessService) {
            showFileChooserDialog(context, target)
            entry
        } else {
            var path = target.path
            if (hasChooser) {
                showFileChooserDialog(context, target)
                path = ""
            }
            Entry.updateDownloadEntry(entry, path)
        }
    }

    private suspend fun fetchRemoteFile(context: Context): File {
        val deferredDialog = showProgressDialogAsync(context)

        val uri: String
        val client: OkHttpClient?
        val output: File

        if (entry.isProcessService) {
            uri = TaskRepository().contentUri(entry)
            client = TaskRepository().getHttpClient()
            output = TaskRepository().getContentDirectory(entry.fileName)
        } else {
            uri = BrowseRepository().contentUri(entry)
            client = null
            output = File(context.cacheDir, entry.name)
        }

        val deferredDownload = GlobalScope.async(Dispatchers.IO) {
            ContentDownloader.downloadFileTo(uri, output.path, client)
        }
        this.deferredDownload.compareAndSet(null, deferredDownload)

        try {
            deferredDownload.await()
        } catch (ex: Exception) {
            output.delete()
            deferredDialog.cancel()
            throw ex
        }
        deferredDialog.cancelAndJoin()

        return output
    }

    private fun showFileChooserDialog(context: Context, file: File) {
        val contentUri = FileProvider.getUriForFile(context, ContentDownloader.FILE_PROVIDER_AUTHORITY, file)
        val intent = Intent(Intent.ACTION_VIEW)
            .setData(contentUri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val chooser = Intent.createChooser(intent, context.getString(R.string.action_open_with_title))
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
        } else {
            throw Action.Exception(context.resources.getString(R.string.action_open_with_error_no_viewer))
        }
    }

    private suspend fun showProgressDialogAsync(context: Context) =
        GlobalScope.async(Dispatchers.Main) {
            suspendCancellableCoroutine<Boolean> {
                val dialog = MaterialAlertDialogBuilder(context)
                    .setTitle(entry.name)
                    .setMessage(
                        context.getString(R.string.action_open_with_downloading)
                    )
                    .setIcon(
                        ResourcesCompat.getDrawable(
                            context.resources,
                            MimeType.with(entry.mimeType).icon,
                            context.theme
                        )
                    )
                    .setNegativeButton(
                        context.getString(R.string.action_open_with_cancel)
                    ) { _, _ ->
                        deferredDownload.get()?.cancel()
                    }
                    .setCancelable(false)
                    .show()
                it.invokeOnCancellation {
                    dialog.dismiss()
                }
            }
        }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) = Unit
}
