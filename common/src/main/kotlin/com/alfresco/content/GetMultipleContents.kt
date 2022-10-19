package com.alfresco.content

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper

/**
 * An ActivityResultContract similar to
 * [androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents]
 * that allows specifying multiple mimeTypes.
 */
class GetMultipleContents : ActivityResultContract<Array<String>, List<Uri>>() {

    @CallSuper
    override fun createIntent(context: Context, input: Array<String>): Intent {
        return Intent(Intent.ACTION_GET_CONTENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("*/*")
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            .putExtra(Intent.EXTRA_MIME_TYPES, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        return if (intent == null || resultCode != Activity.RESULT_OK) {
            emptyList()
        } else getClipDataUris(intent)
    }

    companion object {
        const val MAX_FILE_SIZE = 100

        /**
         * returns true if file exceed the 100mb length otherwise false
         */
        fun isFileSizeExceed(length: Long): Boolean {
            val fileLength = length.div(1024L).div(1024L)
            return fileLength > MAX_FILE_SIZE.minus(1).toLong()
        }

        fun getClipDataUris(intent: Intent): List<Uri> {
            // Use a LinkedHashSet to maintain any ordering that may be
            // present in the ClipData
            val resultSet = LinkedHashSet<Uri>()

            val intentData = intent.data
            if (intentData != null) {
                resultSet.add(intentData)
            }

            val clipData = intent.clipData
            if (clipData == null && resultSet.isEmpty()) {
                return emptyList()
            } else if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    if (uri != null) {
                        resultSet.add(uri)
                    }
                }
            }
            return ArrayList(resultSet)
        }
    }
}
