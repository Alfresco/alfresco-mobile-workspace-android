package com.alfresco.content.actions

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.alfresco.content.withFragment
import kotlinx.coroutines.suspendCancellableCoroutine

class CreateFolderFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private suspend fun openFolderDialog(): CreateFolderDataModel =
        suspendCancellableCoroutine { continuation ->
            setFragmentResultListener(""){
                requestKey, bundle ->
//                val result = bundle.getParcelable<CreateFolderDataModel>()
            }
        }


    companion object {
        private val TAG = CreateFolderFragment::class.java.simpleName

        suspend fun openFolderDialog(
            context: Context
        ): CreateFolderDataModel =
            withFragment(
                context,
                TAG,
                { it.openFolderDialog() },
                { CreateFolderFragment() }
            )

    }

}