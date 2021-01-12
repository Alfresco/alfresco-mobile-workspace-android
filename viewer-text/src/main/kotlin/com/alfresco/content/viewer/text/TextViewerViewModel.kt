package com.alfresco.content.viewer.text

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.session.SessionManager
import com.alfresco.content.viewer.common.ChildViewerArgs
import com.alfresco.download.ContentDownloader
import com.alfresco.kotlin.isLocalPath
import com.alfresco.kotlin.parentFile
import java.io.File
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class TextViewerState(
    val uri: String,
    val path: Async<String> = Uninitialized
) : MvRxState {
    constructor(args: ChildViewerArgs) : this(args.uri)
}

class TextViewerViewModel(
    state: TextViewerState,
    context: Context
) : MvRxViewModel<TextViewerState>(state) {

    val docPath: File

    init {
        if (state.uri.isLocalPath()) {
            docPath = requireNotNull(state.uri.parentFile())
            setState { copy(path = Success(uri)) }
        } else {
            docPath = SessionManager.requireSession.cacheDir
            val output = File(docPath, TMP_FILE_NAME)

            viewModelScope.launch {
                ContentDownloader
                    .downloadFile(state.uri, output.path)
                    .map { "file://$it" }
                    .execute {
                        copy(path = it)
                    }
            }
        }
    }

    companion object : MvRxViewModelFactory<TextViewerViewModel, TextViewerState> {
        private const val TMP_FILE_NAME = "content.tmp"

        override fun create(viewModelContext: ViewModelContext, state: TextViewerState): TextViewerViewModel? {
            return TextViewerViewModel(state, viewModelContext.app())
        }
    }
}
