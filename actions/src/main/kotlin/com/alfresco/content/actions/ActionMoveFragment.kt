package com.alfresco.content.actions

import android.content.Context
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.alfresco.content.actions.MoveResultContract.Companion.ENTRY_OBJ_KEY
import com.alfresco.content.data.Entry
import com.alfresco.content.withNewFragment
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Mark as ActionMoveFragment
 */
class ActionMoveFragment : Fragment() {
    private lateinit var requestLauncher: ActivityResultLauncher<Unit>
    private var onResult: CancellableContinuation<String?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            val entryObj = bundle.getParcelable(ENTRY_OBJ_KEY) as Entry?
            requestLauncher = registerForActivityResult(MoveResultContract(entryObj)) {
                onResult?.resume(it, null)
            }
        }
    }

    private suspend fun moveItems(): String? =
        suspendCancellableCoroutine { continuation ->
            onResult = continuation
            requestLauncher.launch(Unit)
        }

    companion object {
        private val TAG = ActionMoveFragment::class.java.simpleName

        /**
         * Generating ActionMoveFragment
         */
        suspend fun moveItem(
            context: Context,
            entry: Entry,
        ): String? {
            val fragment = ActionMoveFragment()
            val bundle = Bundle().apply {
                putParcelable(ENTRY_OBJ_KEY, entry)
            }
            fragment.arguments = bundle
            return withNewFragment(
                context,
                TAG,
                { it.moveItems() },
                { fragment },
            )
        }
    }
}
