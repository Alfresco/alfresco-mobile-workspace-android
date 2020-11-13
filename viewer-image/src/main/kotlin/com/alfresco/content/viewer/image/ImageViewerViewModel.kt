package com.alfresco.content.viewer.image

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.viewer.common.ChildViewerArgs
import com.alfresco.download.ContentDownloader
import java.io.File
import kotlinx.coroutines.launch

data class ImageViewerState(
    val uri: String,
    val mimeType: String,
    val largeScale: Boolean = largeScaleFormats.contains(mimeType),
    val path: Async<String> = Uninitialized
) : MvRxState {
    constructor(args: ChildViewerArgs) : this(args.uri, args.type)

    companion object {
        private val largeScaleFormats = setOf("image/jpeg", "image/png")
    }
}

class ImageViewerViewModel(
    context: Context,
    state: ImageViewerState
) : MvRxViewModel<ImageViewerState>(state) {

    init {
        if (state.largeScale) {
            val output = File(context.cacheDir, TMP_FILE_NAME)
            viewModelScope.launch {
                ContentDownloader
                    .downloadFile(state.uri, output.path)
                    .execute { copy(path = it) }
            }
        }
    }

    companion object : MvRxViewModelFactory<ImageViewerViewModel, ImageViewerState> {
        private const val TMP_FILE_NAME = "content.tmp"

        override fun create(viewModelContext: ViewModelContext, state: ImageViewerState): ImageViewerViewModel? {
            return ImageViewerViewModel(viewModelContext.app(), state)
        }
    }
}
