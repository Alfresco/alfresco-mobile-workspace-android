package com.alfresco.content.actions

import android.annotation.SuppressLint
import android.view.View
import androidx.annotation.StringRes
import com.alfresco.events.on
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope

/**
 * Mark as ActionMove
 */

interface ActionMove : Action {

    companion object {
        /**
         * Showing toast for the move action
         */
        fun showActionToasts(scope: CoroutineScope, view: View?, anchorView: View? = null) {
            scope.on(block = showToast(view, anchorView))
            scope.on<Action.Error> {
                if (view != null) {
                    showToast(view, anchorView, it.message)
                }
            }
        }

        private fun <T : ActionMove> showToast(view: View?, anchorView: View?): suspend (value: T) -> Unit {
            return { action: T ->
                // Don't call on backstack views
                if (view != null) {
                    action.showToast(view, anchorView)
                }
            }
        }

        internal fun showToast(
            view: View,
            anchorView: View?,
            @StringRes messageResId: Int,
            vararg formatArgs: String
        ) = showToast(
            view,
            anchorView,
            view.resources.getString(
                messageResId, *formatArgs
            )
        )

        @SuppressLint("ShowToast")
        internal fun showToast(
            view: View,
            anchorView: View?,
            message: CharSequence
        ) {
            Snackbar.make(
                view,
                message,
                Snackbar.LENGTH_LONG
            ).setAnchorView(anchorView).show()
        }
    }
}
