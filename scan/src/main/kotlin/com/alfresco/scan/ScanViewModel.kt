package com.alfresco.scan

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.session.SessionManager
import com.alfresco.pdf.CreatePdf
import com.alfresco.pdf.ImageToPDFOptions
import com.alfresco.pdf.OnPDFCreatedInterface
import com.alfresco.pdf.PDFConstants
import com.alfresco.pdf.PDFConstants.DEFAULT_PAGE_SIZE
import java.io.File

/**
 * Marked as ScanState class
 */
data class ScanState(
    val visibleItem: ScanItem? = null,
    val listScanCaptures: List<ScanItem> = emptyList(),
    val listPdf: List<ScanItem> = emptyList()
) : MavericksState

/**
 * Marked as ScanViewModel class
 */
class ScanViewModel(
    val context: Context,
    state: ScanState
) : MavericksViewModel<ScanState>(state), OnPDFCreatedInterface {
    var onPdfCreated: (() -> Unit)? = null

    private val captureDir = SessionManager.requireSession.captureDir
    private val cropDir = SessionManager.requireSession.cropDir
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
            copy(listScanCaptures = emptyList())
        }
    }

    /**
     * preparing the capture file path
     */
    fun prepareCaptureFile() =
        File(captureDir, "${System.currentTimeMillis()}${extensionFor(true)}")

    /**
     * create pdf from images
     */
    fun createPdf() = withState { state ->

        val listOfUri = state.listScanCaptures.map { it.uri.toString() }.toList()

        val imageToPdfOptions = ImageToPDFOptions(
            imagesUri = listOfUri,
            mPageSize = DEFAULT_PAGE_SIZE,
            pageColor = PDFConstants.DEFAULT_PAGE_COLOR,
            mFileName = ScanItem.defaultPdfFilename(),
            imageScaleType = PDFConstants.IMAGE_SCALE_TYPE_ASPECT_RATIO,
            pageNumStyle = PDFConstants.PG_NUM_STYLE_X
        )
        CreatePdf(imageToPdfOptions, captureDir.absolutePath, this@ScanViewModel).execute()
    }

    /**
     * preparing the capture file path
     */
    fun prepareCropFile() =
        File(cropDir, "${System.currentTimeMillis()}${extensionFor(true)}")

    private fun extensionFor(isImage: Boolean) = if (isImage) ScanItem.PHOTO_EXTENSION else ScanItem.PDF_EXTENSION

    /**
     * adding the uri to list after cropping
     */
    fun onCapturePhotoOrPdf(uri: Uri, isImage: Boolean, name: String = "") = if (isImage)
        onCaptureMedia(ScanItem.photoCapture(uri), isImage) else onCaptureMedia(ScanItem.pdfCapture(uri, name), isImage)

    private fun onCaptureMedia(media: ScanItem, isImage: Boolean) =
        setState {
            if (isImage) {
                val list = listScanCaptures + listOf(media)
                copy(listScanCaptures = list)
            } else {
                copy(listPdf = listOf(media))
            }
        }

    override fun onPDFCreated(success: Boolean, path: String?, fileName: String) {

        if (success && !path.isNullOrEmpty()) {
            onCapturePhotoOrPdf(Uri.parse(path), false, fileName)
            onPdfCreated?.invoke()
        }
    }

    companion object : MavericksViewModelFactory<ScanViewModel, ScanState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ScanState
        ) = ScanViewModel(viewModelContext.activity(), state)
    }
}
