package com.alfresco.content

import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

open class PermissionFragment : Fragment() {
    private var continuation: CancellableContinuation<Boolean>? = null

    private fun requestPermissions(
        vararg permissions: String,
        continuation: CancellableContinuation<Boolean>
    ) {
        val isRequestRequired =
            permissions
                .map { ContextCompat.checkSelfPermission(requireContext(), it) }
                .any { result -> result == PackageManager.PERMISSION_DENIED }

        if (isRequestRequired) {
            this.continuation = continuation
            requestPermissions(permissions, REQUEST_ID)
        } else {
            continuation.resume(true) {
                // no-op
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val isGranted = grantResults.all { result -> result == PackageManager.PERMISSION_GRANTED }
        continuation?.resume(isGranted) {
            // no-op
        }
    }

    suspend fun requestPermissions(vararg permissions: String): Boolean =
        suspendCancellableCoroutine {
                continuation -> requestPermissions(*permissions, continuation = continuation)
        }

    companion object {
        private val TAG = PermissionFragment::class.java.simpleName
        private const val REQUEST_ID = 1

        suspend fun requestPermissions(
            context: Context,
            vararg permissions: String
        ): Boolean =
            withContext(Dispatchers.Main) {
                val fragmentManager = when (context) {
                    is AppCompatActivity -> context.supportFragmentManager
                    is Fragment -> context.childFragmentManager
                    else -> throw CancellationException("Context needs to be either AppCompatActivity or Fragment", ClassCastException())
                }

                var fragment = fragmentManager.findFragmentByTag(TAG)
                return@withContext if (fragment != null) {
                    (fragment as PermissionFragment).requestPermissions(*permissions)
                } else {
                    fragment = PermissionFragment()
                    fragmentManager.beginTransaction().add(
                        fragment,
                        TAG
                    ).commitNow()
                    fragment.requestPermissions(*permissions)
                }
            }
    }
}
