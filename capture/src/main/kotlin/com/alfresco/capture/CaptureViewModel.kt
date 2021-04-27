package com.alfresco.capture

import android.content.Context
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.OfflineRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

data class CaptureState(
    val parentId: String,
    val file: String? = null
) : MavericksState {
    constructor(args: CaptureArgs) : this(args.parentId)
}

class CaptureViewModel(
    state: CaptureState,
    context: Context
) : MavericksViewModel<CaptureState>(state) {

    var onSaveComplete: (() -> Unit)? = null

    fun save(filename: String) = withState {
        val extension = ".jpg"
        viewModelScope.launch {
            requireNotNull(it.file)
            OfflineRepository().scheduleForUpload(
                it.file,
                it.parentId,
                filename + extension,
                "",
                "image/jpeg"
            )
            onSaveComplete?.invoke()
        }
    }

    fun capturePhoto(path: String) =
        setState { copy(file = path) }

    fun defaultFilename(): String {
        val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val time: Date = Calendar.getInstance().time
        return "IMG_${formatter.format(time)}"
    }

    fun isFilenameValid(filename: String): Boolean {
        val reservedChars = "?:\"*|/\\<>\u0000"
        return filename.all { c -> reservedChars.indexOf(c) == -1 }
    }

    companion object : MavericksViewModelFactory<CaptureViewModel, CaptureState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: CaptureState
        ) = CaptureViewModel(state, viewModelContext.activity)
    }
}
