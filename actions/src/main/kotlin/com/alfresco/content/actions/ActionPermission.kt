package com.alfresco.content.actions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.alfresco.content.PermissionFragment
import com.alfresco.events.EventBus
import com.alfresco.events.on
import com.google.android.material.snackbar.Snackbar
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Marked as ActionPermission
 */
interface ActionPermission {

    /**
     * It executed when we have read permission granted
     */
    suspend fun executeIntentData(context: Context)

    /**
     * It executed to take the read permission from user.
     */
    fun executePermission(
        context: Context,
        scope: CoroutineScope
    ) = scope.launch {
        val bus = EventBus.default
        try {
            if (checkReadPermission(context))
                executeIntentData(context)
        } catch (ex: CancellationException) {
            println("ExtensionActivity.CancellationException")
            (context as AppCompatActivity).finish()
        } catch (ex: Exception) {
            println("ExtensionActivity.Exception")
            bus.send(Error(ex.message ?: ""))
            delay(1000)
            (context as AppCompatActivity).finish()
        }
    }

    private suspend fun checkReadPermission(context: Context): Boolean {

        if (PermissionFragment.requestPermissions(
                context,
                requiredPermissions(),
                permissionRationale(context)
            )
        ) {
            return true
        } else {
            throw Exception(
                context.getString(R.string.share_files_failure_permissions)
            )
        }
    }

    /**
     * Mark as Error
     */
    class Error(val message: String)

    /**
     * showing toast on execution complete
     */
    fun showToast(view: View, anchorView: View? = null)

    companion object {

        /**
         * show Message on coroutine scope using lifecycle
         */
        fun showActionPermissionToasts(scope: CoroutineScope, view: View?, anchorView: View? = null) {
            scope.on<ActionPermission>(block = showToast(view, anchorView))
            scope.on<Error> {
                if (view != null) {
                    showToast(view, anchorView, it.message)
                }
            }
        }

        private fun <T : ActionPermission> showToast(view: View?, anchorView: View?): suspend (value: T) -> Unit {
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

        private fun requiredPermissions() =
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )

        private fun permissionRationale(context: Context) =
            context.getString(R.string.share_files_permissions_rationale)
    }
}
