package com.alfresco.content

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

class ContentPickerFragment : Fragment() {
    private lateinit var requestLauncher: ActivityResultLauncher<Array<String>>
    private var onResult: CancellableContinuation<List<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestLauncher = registerForActivityResult(GetMultipleContents()) {
            onResult?.resume(it, null)
        }
    }

    private suspend fun pickItems(mimeTypes: Array<String>): List<Uri> =
        suspendCancellableCoroutine { continuation ->
            onResult = continuation
            requestLauncher.launch(mimeTypes)
        }

    companion object {
        private val TAG = ContentPickerFragment::class.java.simpleName

        suspend fun pickItems(
            context: Context,
            mimeTypes: Array<String>,
        ): List<Uri> =
            withFragment(
                context,
                TAG,
                { it.pickItems(mimeTypes) },
                { ContentPickerFragment() },
            )
    }
}
