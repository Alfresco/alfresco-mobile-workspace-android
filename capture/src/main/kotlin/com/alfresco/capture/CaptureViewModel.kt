package com.alfresco.capture

import android.content.Context
import android.location.Location
import android.net.Uri
import androidx.camera.core.ImageCapture
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.Settings
import com.alfresco.content.session.SessionManager
import java.io.File

data class CaptureState(
    val visibleItem: CaptureItem? = null,
    val listCapture: List<CaptureItem> = emptyList()
) : MavericksState

class CaptureViewModel(
    val context: Context,
    state: CaptureState
) : MavericksViewModel<CaptureState>(state) {
    var onSaveComplete: ((List<CaptureItem>) -> Unit)? = null

    var longitude = "0"
    var latitude = "0"
    private val captureDir = SessionManager.requireSession.captureDir
    var mode: CaptureMode = CaptureMode.Photo
    private fun distributionVersion() = Settings(context).getDistributionVersion

    init {
        // Clear any pending captures from a previous session
        clearCaptures()
    }

    fun clearCaptures() {
        captureDir.listFiles()?.forEach { it.delete() }
    }

    fun clearCaptureList() = setState {
        copy(listCapture = listOf())
    }

    fun clearSingleCaptures(captureItem: CaptureItem) {
        captureDir.listFiles()?.forEach {
            if (captureItem.uri.toString().contains(it.name)) {
                it.delete()
            }
        }
        deleteCapture(captureItem)
    }

    private fun deleteCapture(captureItem: CaptureItem) =
        setState {
            copy(listCapture = listCapture.filter {
                it.uri != captureItem.uri
            })
        }

    fun prepareCaptureFile(mode: CaptureMode) =
        File(captureDir, "${System.currentTimeMillis()}${extensionFor(mode)}")

    private fun extensionFor(mode: CaptureMode) =
        when (mode) {
            CaptureMode.Photo -> CaptureItem.PHOTO_EXTENSION
            CaptureMode.Video -> CaptureItem.VIDEO_EXTENSION
        }

    fun save() = withState {
        requireNotNull(it.listCapture)

        it.listCapture.let { capturedList ->
            onSaveComplete?.invoke(
                capturedList
            )
        }
    }

    fun onCapturePhoto(uri: Uri) =
        onCaptureMedia(CaptureItem.photoCapture(uri))

    fun onCaptureVideo(uri: Uri) =
        onCaptureMedia(CaptureItem.videoCapture(uri))

    private fun onCaptureMedia(media: CaptureItem) =
        setState {
            val list = listCapture + listOf(media)
            copy(listCapture = list)
        }

    fun isFilenameValid(filename: String): Boolean {
        val reservedChars = "?:\"*|/\\<>\u0000"
        return filename.all { c -> reservedChars.indexOf(c) == -1 }
    }

    fun isAllFileNameValid(listCapture: List<CaptureItem?>): Boolean {

        var isValidNotEmpty = false

        listCapture.forEach {
            it?.let { capture ->
                val valid = isFilenameValid(capture.name)
                val empty = capture.name.isEmpty()
                isValidNotEmpty = valid && !empty
                if (!isValidNotEmpty)
                    return isValidNotEmpty
            }
        }

        return isValidNotEmpty
    }

    fun updateName(newFileName: String) = withState {
        val newList = it.listCapture.map { captureItem ->
            if (captureItem == it.visibleItem) {
                val updateCapture = captureItem?.copy(name = newFileName)
                setState { copy(visibleItem = updateCapture) }
                updateCapture
            } else {
                captureItem
            }
        }
        setState { copy(listCapture = newList) }
    }

    fun updateDescription(newDescription: String) = withState {
        val newList = it.listCapture.map { captureItem ->
            if (captureItem == it.visibleItem) {
                val updateCapture = captureItem?.copy(description = newDescription)
                setState { copy(visibleItem = updateCapture) }
                updateCapture
            } else {
                captureItem
            }
        }
        setState { copy(listCapture = newList) }
    }

    fun copyVisibleItem(item: CaptureItem) {
        setState {
            copy(visibleItem = item)
        }
    }
    fun getMetaData(): ImageCapture.Metadata {
        val metadata = ImageCapture.Metadata()

        val location = Location("")

        location.longitude = longitude.toDouble()
        location.latitude = latitude.toDouble()

        metadata.location = location

        return metadata
    }

    fun isEnterprise(): Boolean = distributionVersion() == Settings.DistributionVersion.ENTERPRISE

    companion object : MavericksViewModelFactory<CaptureViewModel, CaptureState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: CaptureState
        ) = CaptureViewModel(viewModelContext.activity(), state)
    }
}
