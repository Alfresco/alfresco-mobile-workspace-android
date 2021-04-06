package com.alfresco.capture

import android.content.Context
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.BrowseRepository
import java.io.File
import kotlinx.coroutines.launch

data class CaptureState(
    val parentId: String,
    val files: List<String> = arrayListOf()
) : MavericksState {
    constructor(args: CaptureArgs) : this(args.parentId)
}

class CaptureViewModel(
    state: CaptureState,
    context: Context
) : MavericksViewModel<CaptureState>(state) {

    var onUploadComplete: (() -> Unit)? = null

    fun save(filename: String) = withState {
        val extension = ".jpg"
        viewModelScope.launch {
            for (path in it.files) {
                val file = File(path)
                BrowseRepository().uploadFile(
                    it.parentId,
                    file,
                    filename + extension,
                    "image/jpeg"
                )
                onUploadComplete?.invoke()
            }
        }
    }

    fun capturePhoto(path: String) {
        setState { copy(files = files + path) }
    }

    companion object : MavericksViewModelFactory<CaptureViewModel, CaptureState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: CaptureState
        ) = CaptureViewModel(state, viewModelContext.activity)
    }
}
