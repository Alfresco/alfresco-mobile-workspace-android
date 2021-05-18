package com.alfresco.capture

import android.content.Context
import android.net.Uri
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.session.SessionManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CaptureState(
    val file: String? = null
) : MavericksState

class CaptureViewModel(
    state: CaptureState,
    context: Context
) : MavericksViewModel<CaptureState>(state) {

    var onSaveComplete: ((CaptureItem) -> Unit)? = null
    val captureDir = SessionManager.requireSession.captureDir

    init {
        // Clear any pending captures from a previous session
        clearCaptures()
    }

    fun clearCaptures() {
        captureDir.listFiles()?.forEach { it.delete() }
    }

    fun prepareCaptureFile(outputDir: File, extension: String) =
        File(outputDir, "${System.currentTimeMillis() / 1000}$extension")

    fun save(
        filename: String,
        description: String
    ) = withState {
        requireNotNull(it.file)

        onSaveComplete?.invoke(CaptureItem(
            Uri.parse(it.file),
            filename + PHOTO_EXTENSION,
            description,
            PHOTO_MIMETYPE
        ))
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
        const val PHOTO_EXTENSION = ".jpg"
        const val PHOTO_MIMETYPE = "image/jpeg"

        override fun create(
            viewModelContext: ViewModelContext,
            state: CaptureState
        ) = CaptureViewModel(state, viewModelContext.activity)
    }
}
