package com.alfresco.content

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

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
            withContext(Dispatchers.Main) {
                findPermissionFragment(context).requestPermission(permission)
            }

        private fun findPermissionFragment(context: Context): PermissionFragment {
            val fragmentManager = when (context) {
                is AppCompatActivity -> context.supportFragmentManager
                is Fragment -> context.childFragmentManager
                else -> throw CancellationException("Context needs to be either AppCompatActivity or Fragment", ClassCastException())
            }

            var fragment = fragmentManager.findFragmentByTag(TAG)
            if (fragment != null) {
                (fragment as PermissionFragment)
            } else {
                fragment = PermissionFragment()
                fragmentManager.beginTransaction().add(
                    fragment,
                    TAG
                ).commitNow()
            }
            return fragment
        }

        fun hasPermission(context: Context, permission: String) =
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
