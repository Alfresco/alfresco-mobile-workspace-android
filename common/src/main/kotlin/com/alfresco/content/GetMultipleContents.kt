package com.alfresco.content

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper
import java.util.ArrayList
import java.util.LinkedHashSet

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
