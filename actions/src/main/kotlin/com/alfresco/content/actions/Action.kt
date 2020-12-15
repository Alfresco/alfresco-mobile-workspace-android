package com.alfresco.content.actions

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import com.alfresco.content.data.Entry
import com.google.android.material.snackbar.Snackbar
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface Action {
    val entry: Entry
    val icon: Int
    val title: Int

    suspend fun execute(context: Context): Entry
    fun copy(_entry: Entry): Action

    fun execute(
        context: Context,
        scope: CoroutineScope
    ) = scope.launch {
        val bus = EventBus.default
        try {
            val newEntry = execute(context)
            val newAction = copy(newEntry)
            bus.send(newAction)
        } catch (ex: CancellationException) {
            // no-op
        } catch (ex: Exception) {
            bus.send(Error(ex.message ?: ""))
        } catch (ex: SocketTimeoutException) {
            bus.send(Error(context.getString(R.string.action_timeout_error)))
        } catch (ex: kotlin.Exception) {
            bus.send(Error(context.getString(R.string.action_generic_error)))
        }
    }

    fun showToast(view: View, anchorView: View? = null)

    fun maxFileNameInToast(view: View) =
        view.context.resources.getInteger(R.integer.action_toast_file_name_max_length)

    data class Error(val message: String)

    class Exception(string: String) : kotlin.Exception(string)

    companion object {
        fun showActionToasts(scope: CoroutineScope, view: View?, anchorView: View? = null) {
            scope.on<ActionAddFavorite> (block = showToast(view, anchorView))
            scope.on<ActionRemoveFavorite> (block = showToast(view, anchorView))
            scope.on<ActionDelete> (block = showToast(view, anchorView))
            scope.on<ActionRestore> (block = showToast(view, anchorView))
            scope.on<ActionDeleteForever> (block = showToast(view, anchorView))
            scope.on<ActionDownload> (block = showToast(view, anchorView))
            scope.on<ActionAddOffline> (block = showToast(view, anchorView))
            scope.on<ActionRemoveOffline> (block = showToast(view, anchorView))
            scope.on<Error> {
                if (view != null) {
                    showToast(view, anchorView, it.message)
                }
            }
        }

        private fun <T : Action> showToast(view: View?, anchorView: View?): suspend (value: T) -> Unit {
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
                view.resources.getString(messageResId, *formatArgs
            ))

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
