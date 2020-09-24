package com.alfresco.content.viewer.text

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.viewer.common.ContentDownloader
import com.alfresco.content.viewer.common.ViewerTypeArgs
import java.io.File
import java.lang.Exception
import kotlinx.coroutines.launch

data class TextViewerState(
    val uri: String,
    val path: String? = null
) : MvRxState {
    constructor(args: ViewerTypeArgs) : this(args.uri)
}

class TextViewerViewModel(
    state: TextViewerState,
    context: Context
) : MvRxViewModel<TextViewerState>(state) {

    init {
        val output = File(context.cacheDir, TMP_FILE_NAME)

        viewModelScope.launch {
            try {
                ContentDownloader.downloadFileTo(state.uri, output.path)
                setState { copy(path = "file://${output.path}") }
            } catch (_: Exception) {
                // no-op
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
