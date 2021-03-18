package com.alfresco.content.viewer.text

import android.content.Context
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
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
) : MavericksState {
    constructor(args: ChildViewerArgs) : this(args.uri)
}

class TextViewerViewModel(
    state: TextViewerState,
    context: Context
) : MavericksViewModel<TextViewerState>(state) {

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

    companion object : MavericksViewModelFactory<TextViewerViewModel, TextViewerState> {
        private const val TMP_FILE_NAME = "content.tmp"

        override fun create(
            viewModelContext: ViewModelContext,
            state: TextViewerState
        ) = TextViewerViewModel(state, viewModelContext.app())
    }
}
