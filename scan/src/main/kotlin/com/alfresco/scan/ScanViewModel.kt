package com.alfresco.scan

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.session.SessionManager
import java.io.File

/**
 * Marked as ScanState class
 */
data class ScanState(
    val visibleItem: ScanItem? = null,
    val listCapture: List<ScanItem> = emptyList()
) : MavericksState

/**
 * Marked as ScanViewModel class
 */
class ScanViewModel(
    val context: Context,
    state: ScanState
) : MavericksViewModel<ScanState>(state) {
    var onSaveComplete: ((List<ScanItem>) -> Unit)? = null

    private val captureDir = SessionManager.requireSession.captureDir
    var flashMode = ImageCapture.FLASH_MODE_AUTO
    var lensFacing = -1
    var uri: Uri? = null

    init {
        // Clear any pending captures from a previous session
        clearCaptures()
    }

    /**
     * clear all the captured scans
     */
    fun clearCaptures() {
        captureDir.listFiles()?.forEach { it.delete() }
        setState {
            copy(listCapture = emptyList())
        }
    }

    /**
     * preparing the capture file path
     */
    fun prepareCaptureFile() =
        File(captureDir, "${System.currentTimeMillis()}${extensionFor()}")

    private fun extensionFor() = ScanItem.PHOTO_EXTENSION

    /**
     * adding the uri to list after cropping
     */
    fun onCapturePhoto(uri: Uri) =
        onCaptureMedia(ScanItem.photoCapture(uri))

    private fun onCaptureMedia(media: ScanItem) =
        setState {
            val list = listCapture + listOf(media)
            copy(listCapture = list)
        }

    companion object : MavericksViewModelFactory<ScanViewModel, ScanState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ScanState
        ) = ScanViewModel(viewModelContext.activity(), state)
    }
}
