package com.alfresco.content.actions

import android.content.Context
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.alfresco.content.withFragment
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

        requestLauncher = registerForActivityResult(MoveResultContract()) {
            onResult?.resume(it, null)
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
            context: Context
        ): String? =
            withFragment(
                context,
                TAG,
                { it.moveItems() },
                { ActionMoveFragment() }
            )
    }
}
