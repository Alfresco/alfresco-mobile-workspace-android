package com.alfresco.content.viewer.image

import android.content.Context
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.viewer.common.ChildViewerArgs
import com.alfresco.download.ContentDownloader
import com.alfresco.kotlin.isLocalPath
import kotlinx.coroutines.launch
import java.io.File

data class ImageViewerState(
    val uri: String,
    val mimeType: String,
    val largeScale: Boolean = largeScaleFormats.contains(mimeType),
    val path: Async<String> = Uninitialized,
) : MavericksState {
    constructor(args: ChildViewerArgs) : this(args.uri, args.type)

    companion object {
        private val largeScaleFormats = setOf("image/jpeg", "image/png")
    }
}

class ImageViewerViewModel(
    state: ImageViewerState,
    context: Context,
) : MavericksViewModel<ImageViewerState>(state) {
    init {
        if (state.largeScale && !state.uri.isLocalPath()) {
            val output = File(context.cacheDir, TMP_FILE_NAME)
            viewModelScope.launch {
                ContentDownloader
                    .downloadFile(state.uri, output.path)
                    .execute { copy(path = it) }
            }
        } else {
            setState { copy(path = Success(uri)) }
        }
    }

    companion object : MavericksViewModelFactory<ImageViewerViewModel, ImageViewerState> {
        private const val TMP_FILE_NAME = "content.tmp"

        override fun create(
            viewModelContext: ViewModelContext,
            state: ImageViewerState,
        ) = ImageViewerViewModel(state, viewModelContext.app())
    }
}
