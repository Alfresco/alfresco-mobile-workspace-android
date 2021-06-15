package com.alfresco.capture

import android.net.Uri
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.alfresco.content.session.SessionManager
import java.io.File

data class CaptureState(
    val capture: CaptureItem? = null
) : MavericksState

class CaptureViewModel(
    state: CaptureState
) : MavericksViewModel<CaptureState>(state) {

    var onSaveComplete: ((CaptureItem) -> Unit)? = null
    private val captureDir = SessionManager.requireSession.captureDir

    init {
        // Clear any pending captures from a previous session
        clearCaptures()
    }

    fun clearCaptures() {
        captureDir.listFiles()?.forEach { it.delete() }
    }

    fun prepareCaptureFile(mode: CaptureMode) =
        File(captureDir, "${System.currentTimeMillis() / 1000}${extensionFor(mode)}")

    private fun extensionFor(mode: CaptureMode) =
        when (mode) {
            CaptureMode.Photo -> CaptureItem.PHOTO_EXTENSION
            CaptureMode.Video -> CaptureItem.VIDEO_EXTENSION
        }

    fun save(
        filename: String,
        description: String
    ) = withState {
        requireNotNull(it.capture)

        onSaveComplete?.invoke(
            it.capture.copy(
                name = filename,
                description = description
            )
        )
    }

    fun onCapturePhoto(uri: Uri) =
        onCaptureMedia(CaptureItem.photoCapture(uri))

    fun onCaptureVideo(uri: Uri) =
        onCaptureMedia(CaptureItem.videoCapture(uri))

    private fun onCaptureMedia(media: CaptureItem) =
        setState { copy(capture = media) }

    fun isFilenameValid(filename: String): Boolean {
        val reservedChars = "?:\"*|/\\<>\u0000"
        return filename.all { c -> reservedChars.indexOf(c) == -1 }
    }
}
