package com.alfresco.content.actions

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import com.alfresco.Logger
import com.alfresco.content.GetMultipleContents.Companion.MAX_FILE_SIZE_10
import com.alfresco.content.GetMultipleContents.Companion.MAX_FILE_SIZE_100
import com.alfresco.content.data.APIEvent
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.UploadServerType
import com.alfresco.events.EventBus
import com.alfresco.events.on
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

interface Action {
    val entry: ParentEntry
    val entries: List<Entry>
        get() = emptyList()
    val icon: Int
    val title: Int
    val eventName: EventName

    suspend fun execute(context: Context): ParentEntry
    suspend fun executeMulti(context: Context): Pair<ParentEntry, List<Entry>> {
        return Pair(entry, entries)
    }

    fun copy(_entry: ParentEntry): Action
    fun copy(_entries: List<Entry>): Action {
        return this
    }

    fun execute(
        context: Context,
        scope: CoroutineScope,
    ) = scope.launch {
        val bus = EventBus.default
        try {
            val newEntry = execute(context)
            val newAction = copy(newEntry)
            sendAnalytics(true)
            bus.send(newAction)
        } catch (ex: CancellationException) {
            // no-op
            when {
                entry is Entry && (entry as Entry).uploadServer == UploadServerType.UPLOAD_TO_TASK &&
                    ex.message == ERROR_FILE_SIZE_EXCEED -> {
                    bus.send(Error(context.getString(R.string.error_file_size_exceed, MAX_FILE_SIZE_100)))
                }

                entry is Entry && (entry as Entry).uploadServer == UploadServerType.UPLOAD_TO_PROCESS &&
                    ex.message == ERROR_FILE_SIZE_EXCEED -> {
                    bus.send(Error(context.getString(R.string.error_file_size_exceed, MAX_FILE_SIZE_10)))
                }
            }
        } catch (ex: Exception) {
            sendAnalytics(false)
            bus.send(Error(ex.message ?: ""))
        } catch (ex: SocketTimeoutException) {
            sendAnalytics(false)
            bus.send(Error(context.getString(R.string.action_timeout_error)))
        } catch (ex: kotlin.Exception) {
            sendAnalytics(false)
            Logger.e(ex)
            when (title) {
                R.string.action_create_folder -> {
                    if (ex.message?.contains("409") == true) {
                        bus.send(Error(context.getString(R.string.error_duplicate_folder)))
                    }
                }

                else -> bus.send(Error(context.getString(R.string.action_generic_error)))
            }
        }
    }

    fun executeMulti(
        context: Context,
        scope: CoroutineScope,
    ) = scope.launch(Dispatchers.IO) {
        val bus = EventBus.default
        try {
            val newEntry = executeMulti(context)
            val newAction = copy(newEntry.second)
            sendAnalytics(true)
            bus.send(newAction)
        } catch (ex: CancellationException) {
            // no-op
            if (entry is Entry && (entry as Entry).uploadServer == UploadServerType.UPLOAD_TO_TASK &&
                ex.message == ERROR_FILE_SIZE_EXCEED
            ) {
                bus.send(Error(context.getString(R.string.error_file_size_exceed, MAX_FILE_SIZE_100)))
            }
        } catch (ex: Exception) {
            sendAnalytics(false)
            bus.send(Error(ex.message ?: ""))
        } catch (ex: SocketTimeoutException) {
            sendAnalytics(false)
            bus.send(Error(context.getString(R.string.action_timeout_error)))
        } catch (ex: kotlin.Exception) {
            sendAnalytics(false)
            Logger.e(ex)
            when (title) {
                R.string.action_create_folder -> {
                    if (ex.message?.contains("409") == true) {
                        bus.send(Error(context.getString(R.string.error_duplicate_folder)))
                    }
                }

                else -> bus.send(Error(context.getString(R.string.action_generic_error)))
            }
        }
    }

    private fun sendAnalytics(status: Boolean) {
        if (title == R.string.action_create_folder) {
            AnalyticsManager().apiTracker(APIEvent.NewFolder, status)
        }
    }

    /**
     * returns the parent ID on the basis of uploading server
     */
    fun getParentId(entry: Entry): String {
        return when (entry.uploadServer) {
            UploadServerType.DEFAULT -> entry.id
            UploadServerType.UPLOAD_TO_TASK, UploadServerType.UPLOAD_TO_PROCESS -> entry.parentId ?: ""
            else -> ""
        }
    }

    fun showToast(view: View, anchorView: View? = null) {}

    fun maxFileNameInToast(view: View) =
        view.context.resources.getInteger(R.integer.action_toast_file_name_max_length)

    data class Error(val message: String)

    class Exception(string: String) : kotlin.Exception(string)

    companion object {
        const val ERROR_FILE_SIZE_EXCEED = "File size exceed"
        fun showActionToasts(scope: CoroutineScope, view: View?, anchorView: View? = null) {
            scope.on<Action>(block = showToast(view, anchorView))
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
            vararg formatArgs: String,
        ) = showToast(
            view,
            anchorView,
            view.resources.getString(
                messageResId,
                *formatArgs,
            ),
        )

        @SuppressLint("ShowToast")
        internal fun showToast(
            view: View,
            anchorView: View?,
            message: CharSequence,
        ) {
            Snackbar.make(
                view,
                message,
                Snackbar.LENGTH_LONG,
            ).setAnchorView(anchorView).show()
        }
    }
}
