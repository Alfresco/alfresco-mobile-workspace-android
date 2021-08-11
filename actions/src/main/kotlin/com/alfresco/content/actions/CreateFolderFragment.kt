package com.alfresco.content.actions

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import com.alfresco.capture.CaptureItem
import com.alfresco.content.withFragment
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

class CreateFolderFragment : Fragment() {

    private var onResult: CancellableContinuation<CreateFolderDataModel?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(REQUEST_KEY){
                _, bundle ->
            val result = bundle.getParcelable<CreateFolderDataModel>(DATA_OBJ)
            onResult?.resume(result,null)
        }

    }

    private suspend fun openFolderDialog(): CreateFolderDataModel? =
        suspendCancellableCoroutine { continuation ->
            onResult = continuation
            CreateFolderDialog().show(parentFragmentManager, null)
        }


    companion object {
        private val TAG = CreateFolderFragment::class.java.simpleName
        const val REQUEST_KEY="request_key"
        const val DATA_OBJ="data_obj"

        suspend fun openFolderDialog(
            context: Context
        ): CreateFolderDataModel? =
            withFragment(
                context,
                TAG,
                { it.openFolderDialog() },
                { CreateFolderFragment() }
            )

    }

}