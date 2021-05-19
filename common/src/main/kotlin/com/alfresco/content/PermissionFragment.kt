package com.alfresco.content

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

open class PermissionFragment : Fragment() {

    private lateinit var requestLauncher: ActivityResultLauncher<String>
    private var requestCallback: ((Boolean) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register for result is not allowed after onCreate
        requestLauncher = registerForActivityResult(
            RequestPermission()) { isGranted: Boolean ->
            requestCallback?.invoke(isGranted)
        }
    }

    private fun requestPermission(
        permission: String,
        continuation: CancellableContinuation<Boolean>
    ) {
        when {
            hasPermission(requireContext(), permission) -> {
                continuation.resume(true, null)
            }

            shouldShowRequestPermissionRationale(permission) -> {
                continuation.resume(false, null)
            }

            else -> {
                launchPermissionRequest(permission) { isGranted: Boolean ->
                        continuation.resume(isGranted, null)
                    }
            }
        }
    }
    private fun launchPermissionRequest(permission: String, callback: (Boolean) -> Unit) {
        requestCallback = callback
        requestLauncher.launch(permission)
    }

    private suspend fun requestPermission(permission: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            requestPermission(permission, continuation)
        }

    companion object {
        private val TAG = PermissionFragment::class.java.simpleName

        suspend fun requestPermission(
            context: Context,
            permission: String
        ): Boolean =
            withFragment(
                context,
                TAG,
                { it.requestPermission(permission) },
                { PermissionFragment() }
            )

        fun hasPermission(context: Context, permission: String) =
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
