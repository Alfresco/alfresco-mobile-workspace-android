package com.alfresco.capture

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.alfresco.content.withFragment
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

class CaptureHelperFragment : Fragment() {
    private lateinit var requestLauncher: ActivityResultLauncher<Unit>
    private var onResult: CancellableContinuation<List<CaptureItem>?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestLauncher =
            registerForActivityResult(CapturePhotoResultContract()) {
//            onResult?.resume(it, null)
                onResult?.let { continuation ->
                    continuation.resume(it) { cause, _, _ ->
                        // Send null if cancelled
                        if (cause is CancellationException) {
                            continuation.resume(null)
                        }
                    }
                }
            }
    }

    private suspend fun capturePhoto(): List<CaptureItem>? =
        suspendCancellableCoroutine { continuation ->
            onResult = continuation
            requestLauncher.launch(Unit)
        }

    companion object {
        private val TAG = CaptureHelperFragment::class.java.simpleName

        suspend fun capturePhoto(context: Context): List<CaptureItem>? =
            withFragment(
                context,
                TAG,
                { it.capturePhoto() },
                { CaptureHelperFragment() },
            )

        fun requiredPermissions() =
            listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
            )

        fun optionalPermissions() =
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )

        fun permissionRationale(context: Context) = context.getString(R.string.capture_permissions_rationale)
    }
}
