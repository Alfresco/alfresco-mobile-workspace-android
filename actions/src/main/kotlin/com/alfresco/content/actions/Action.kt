package com.alfresco.content.actions

import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import com.alfresco.content.data.Entry
import com.alfresco.content.data.FavoritesRepository
import com.google.android.material.snackbar.Snackbar
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
        scope: CoroutineScope,
        block: (suspend CoroutineScope.(action: Action) -> Unit)? = null
    ) = scope.launch {
        try {
            val newEntry = execute(context)
            val newAction = copy(newEntry)
            EventBus.default.send(newAction)

            if (block != null) {
                block(newAction)
            }
        } catch (ex: CancellationException) {
            // no-op
        } catch (ex: Exception) {
            EventBus.default.send(Error(ex.message ?: ""))
        }
    }

    fun showToast(view: View, anchorView: View? = null)

    data class AddFavorite(
        override val entry: Entry,
        override val icon: Int = R.drawable.ic_favorite,
        override val title: Int = R.string.action_add_favorite_title
    ) : Action {
        private val repository: FavoritesRepository = FavoritesRepository()

        override suspend fun execute(context: Context): Entry {
            repository.addFavorite(entry)
            return entry.copy(isFavorite = true)
        }

        override fun copy(_entry: Entry): Action = copy(entry = _entry)

        override fun showToast(view: View, anchorView: View?) =
            showToast(view, anchorView, R.string.action_add_favorite_toast)
    }

    data class RemoveFavorite(
        override var entry: Entry,
        override val icon: Int = R.drawable.ic_favorite_filled,
        override val title: Int = R.string.action_remove_favorite_title
    ) : Action {
        private val repository: FavoritesRepository = FavoritesRepository()

        override suspend fun execute(context: Context): Entry {
            try {
                repository.removeFavorite(entry)
            } catch (ex: KotlinNullPointerException) {
                // no-op. expected for 204
            }
            return entry.copy(isFavorite = false)
        }

        override fun copy(_entry: Entry): Action = copy(entry = _entry)

        override fun showToast(view: View, anchorView: View?) =
            showToast(view, anchorView, R.string.action_remove_favorite_toast)
    }

    data class Download(
        override var entry: Entry,
        override val icon: Int = R.drawable.ic_download,
        override val title: Int = R.string.action_download_title
    ) : Action {

        override suspend fun execute(context: Context): Entry {
            // TODO:
            return entry
        }

        override fun copy(_entry: Entry): Action = copy(entry = _entry)

        override fun showToast(view: View, anchorView: View?) {
            // no-op
        }
    }

    data class Error(val message: String)

    companion object {
        fun showActionToasts(scope: CoroutineScope, view: View?, anchorView: View? = null) {
            scope.on<AddFavorite> (block = showToast(view, anchorView))
            scope.on<RemoveFavorite> (block = showToast(view, anchorView))
            scope.on<ActionDelete> (block = showToast(view, anchorView))
            scope.on<Error> {
                if (view != null) {
                    showToast(view, anchorView, R.string.action_generic_error)
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
            @StringRes messageResId: Int
        ) {
            Snackbar.make(
                view,
                messageResId,
                Snackbar.LENGTH_LONG
            ).setAnchorView(anchorView).show()
        }
    }
}
