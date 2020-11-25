package com.alfresco.content.actions.create

import android.content.Context
import android.view.View
import com.alfresco.content.actions.Action
import com.alfresco.content.actions.R
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.mimetype.MimeType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

data class ActionCreateDocument(
    override val entry: Entry,
    val type: Type
) : Action {

    override val icon: Int = when (type) {
        Type.Document -> MimeType.MS_DOC.icon
        Type.Spreadsheet -> MimeType.MS_SHEET.icon
        Type.Presentation -> MimeType.MS_PPT.icon
    }

    override val title = when (type) {
        Type.Document -> R.string.action_create_document_title
        Type.Spreadsheet -> R.string.action_create_spreadsheet_title
        Type.Presentation -> R.string.action_create_presentation_title
    }

    override suspend fun execute(context: Context): Entry {
        val file = File(context.cacheDir, "new.docx")
        withContext(Dispatchers.IO) {
            copyAsset(context, template, file)
        }
        return BrowseRepository().uploadFile(entry.id, file, contentType ?: "")
    }

    private suspend fun showCreateDialog(context: Context) =
        GlobalScope.async(Dispatchers.Main) {
            suspendCancellableCoroutine<Boolean> {
                val dialog = MaterialAlertDialogBuilder(context)
                    .setTitle("")
                    .setNegativeButton(context.getString(R.string.action_create_document_negative)) { _, _ ->
                    }
                    .setPositiveButton(context.getString(R.string.action_create_document_positive)) {
                    }
                    .show()
                it.invokeOnCancellation {
                    dialog.dismiss()
                }
            }
        }

    private val template = when (type) {
        Type.Document -> "blank.docx"
        Type.Spreadsheet -> "blank.xlsx"
        Type.Presentation -> "blank.pptx"
    }

    private val contentType = when (type) {
        Type.Document -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        Type.Spreadsheet -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        Type.Presentation -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    }

    @Throws(IOException::class)
    private fun copyAsset(context: Context, src: String, dst: File?) {
        val inputStream = context.assets.open(src)
        val outputStream = FileOutputStream(dst)

        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        inputStream.close()
        outputStream.flush()
        outputStream.close()
    }

    override fun copy(_entry: Entry): Action = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) {
        TODO("Not yet implemented")
    }

    enum class Type {
        Document,
        Spreadsheet,
        Presentation
    }
}
