package com.alfresco.scan

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.alfresco.content.withFragment
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Marked as ScanHelperFragment class
 */
class ScanHelperFragment : Fragment() {
    private lateinit var requestLauncher: ActivityResultLauncher<Unit>
    private var onResult: CancellableContinuation<List<ScanItem>?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestLauncher = registerForActivityResult(ScanResultContract()) {
            onResult?.resume(it, null)
        }
    }

    private suspend fun scanDocuments(): List<ScanItem>? =
        suspendCancellableCoroutine { continuation ->
            onResult = continuation
            requestLauncher.launch(Unit)
        }

    companion object {
        private val TAG = ScanHelperFragment::class.java.simpleName

        /**
         * returns the scanned documents list
         */
        suspend fun scanDocuments(
            context: Context
        ): List<ScanItem>? =
            withFragment(
                context,
                TAG,
                { it.scanDocuments() },
                { ScanHelperFragment() }
            )

        /**
         * Define the required permission for scan documents
         */
        fun requiredPermissions() =
            listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )

        /**
         * Rationale message for the permission
         */
        fun permissionRationale(context: Context) =
            context.getString(R.string.scan_permissions_rationale)
    }
}
