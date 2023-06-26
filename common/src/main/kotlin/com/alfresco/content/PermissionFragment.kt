package com.alfresco.content

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.alfresco.content.common.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

open class PermissionFragment : Fragment() {

    private lateinit var requestLauncher: ActivityResultLauncher<Array<String>>
    private var requestCallback: ((Map<String, Boolean>) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register for result is not allowed after onCreate
        requestLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) {
            requestCallback?.invoke(it)
        }
    }

    private fun requestPermissions(
        permissions: List<String>,
        rationale: String,
        continuation: CancellableContinuation<Boolean>,
    ) {
        val denied = deniedPermissions(requireContext(), permissions)
        when {
            denied.isEmpty() -> {
                continuation.resume(true, null)
            }

            shouldShowRequestRationale(denied) -> {
                showRationaleDialog(rationale, denied, continuation)
            }

            else -> {
                execPermissionRequest(denied, continuation)
            }
        }
    }

    private fun requestOptionalPermissions(
        permissions: List<String>,
        continuation: CancellableContinuation<Boolean>,
    ) {
        val denied = deniedPermissions(requireContext(), permissions)

        when {
            denied.isEmpty() -> {
                continuation.resume(true, null)
            }

            else -> {
                execPermissionRequest(denied, continuation)
            }
        }
    }

    private fun shouldShowRequestRationale(permissions: List<String>) =
        permissions.any { shouldShowRequestPermissionRationale(it) }

    private fun showRationaleDialog(
        rationale: String,
        permissions: List<String>,
        continuation: CancellableContinuation<Boolean>,
    ) = requireActivity().runOnUiThread {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.permission_rationale_title))
            .setMessage(rationale)
            .setNegativeButton(resources.getString(R.string.permission_rationale_negative)) { _, _ ->
                continuation.cancel()
            }
            .setPositiveButton(resources.getString(R.string.permission_rationale_positive)) { _, _ ->
                execPermissionRequest(permissions, continuation)
            }
            .show()
    }

    private fun execPermissionRequest(
        permissions: List<String>,
        continuation: CancellableContinuation<Boolean>,
    ) {
        requestCallback = { response ->
            continuation.resume(response.values.all { it }, null)
        }
        requestLauncher.launch(permissions.toTypedArray())
    }

    private suspend fun requestPermissions(
        permission: List<String>,
        rationale: String,
    ): Boolean = suspendCancellableCoroutine { requestPermissions(permission, rationale, it) }

    private suspend fun requestOptionalPermissions(
        permission: List<String>,
    ): Boolean = suspendCancellableCoroutine { requestOptionalPermissions(permission, it) }

    companion object {
        private val TAG = PermissionFragment::class.java.simpleName

        suspend fun requestPermission(
            context: Context,
            permission: String,
            rationale: String,
        ): Boolean = requestPermissions(context, listOf(permission), rationale)

        suspend fun requestPermissions(
            context: Context,
            permissions: List<String>,
            rationale: String,
        ): Boolean =
            withFragment(
                context,
                TAG,
                { it.requestPermissions(permissions.toList(), rationale) },
                { PermissionFragment() },
            )

        suspend fun requestOptionalPermissions(
            context: Context,
            permissions: List<String>,
        ): Boolean =
            withFragment(
                context,
                TAG,
                { it.requestOptionalPermissions(permissions.toList()) },
                { PermissionFragment() },
            )

        fun deniedPermissions(context: Context, permissions: List<String>) =
            permissions.filter {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_DENIED
            }
    }
}
