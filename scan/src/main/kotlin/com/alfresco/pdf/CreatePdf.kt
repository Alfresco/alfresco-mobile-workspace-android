package com.alfresco.pdf

import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import com.alfresco.scan.ScanItem
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * An coroutine task that converts selected images to Pdf
 */
class CreatePdf(
    mImageToPDFOptions: ImageToPDFOptions,
    parentPath: String,
    onPDFCreated: OnPDFCreatedInterface
) : CoroutineScope {

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job // to run code in Main(UI) Thread

    private val mFileName: String
    private val mQualityString: String?
    private val mImagesUri: List<String>
    private val mBorderWidth: Int
    private val mOnPDFCreatedInterface: OnPDFCreatedInterface
    private var mSuccess = false
    private var mPath: String
    private val mPageSize: String
    private val mMarginTop: Int
    private val mMarginBottom: Int
    private val mMarginRight: Int
    private val mMarginLeft: Int
    private val mImageScaleType: String?
    private val mPageNumStyle: String?
    private val mPageColor: Int

    init {
        mImagesUri = mImageToPDFOptions.imagesUri
        mFileName = mImageToPDFOptions.outFileName.toString()
        mQualityString = mImageToPDFOptions.qualityString
        mOnPDFCreatedInterface = onPDFCreated
        mPageSize = mImageToPDFOptions.pageSize.toString()
        mBorderWidth = mImageToPDFOptions.borderWidth
        mMarginTop = mImageToPDFOptions.marginTop
        mMarginBottom = mImageToPDFOptions.marginBottom
        mMarginRight = mImageToPDFOptions.marginRight
        mMarginLeft = mImageToPDFOptions.marginLeft
        mImageScaleType = mImageToPDFOptions.imageScaleType
        mPageNumStyle = mImageToPDFOptions.pageNumStyle
        mPageColor = mImageToPDFOptions.pageColor
        mPath = parentPath
    }

    /**
     * execute the task to convert images into Pdf
     */
    fun execute() = launch {
        onPreExecute()
        val convertImagesToPdf = async { doInBackground() }
        convertImagesToPdf.await()
        onPostExecute()
    }

    /**
     * call this method to cancel a coroutine when you don't need it anymore,
     * e.g. when user closes the screen
     */
    fun cancel() {
        job.cancel()
    }

    private fun onPreExecute() {
        mSuccess = true
    }

    private fun setFilePath() {
        val folder = File(mPath)
        if (!folder.exists()) folder.mkdir()
        mPath = mPath + "/" + mFileName + ScanItem.PDF_EXTENSION
    }

    private fun doInBackground() {
        setFilePath()
        Log.v("stage 1", "store the pdf in sd card")
        val pageSize = Rectangle(PageSize.getRectangle(mPageSize))
        pageSize.backgroundColor = getBaseColor(mPageColor)
        val document = Document(
            pageSize,
            mMarginLeft.toFloat(), mMarginRight.toFloat(), mMarginTop.toFloat(), mMarginBottom.toFloat()
        )
        Log.v("stage 2", "Document Created")
        document.setMargins(mMarginLeft.toFloat(), mMarginRight.toFloat(), mMarginTop.toFloat(), mMarginBottom.toFloat())
        val documentRect = document.pageSize
        try {
            val writer = PdfWriter.getInstance(document, FileOutputStream(mPath))
            Log.v("Stage 3", "Pdf writer")
            document.open()
            Log.v("Stage 4", "Document opened")
            for (i in mImagesUri.indices) {
                var quality: Int
                quality = 30
                if (!mQualityString.isNullOrEmpty()) {
                    quality = mQualityString.trim().toInt()
                }
                val image = Image.getInstance(mImagesUri[i])
                // compressionLevel is a value between 0 (best speed) and 9 (best compression)
                val qualityMod = quality * 0.09
                image.compressionLevel = qualityMod.toInt()
                image.border = Rectangle.BOX
                image.borderWidth = mBorderWidth.toFloat()
                Log.v("Stage 5", "Image compressed $qualityMod")
                val bmOptions = BitmapFactory.Options()
                val bitmap = BitmapFactory.decodeFile(mImagesUri[i], bmOptions)
                Log.v("Stage 6", "Image path adding")
                val pageWidth = document.pageSize.width - (mMarginLeft + mMarginRight)
                val pageHeight = document.pageSize.height - (mMarginBottom + mMarginTop)
                if (mImageScaleType == PDFConstants.IMAGE_SCALE_TYPE_ASPECT_RATIO) image.scaleToFit(pageWidth, pageHeight) else image.scaleAbsolute(pageWidth, pageHeight)
                image.setAbsolutePosition(
                    (documentRect.width - image.scaledWidth) / 2,
                    (documentRect.height - image.scaledHeight) / 2
                )
                Log.v("Stage 7", "Image Alignments")
                addPageNumber(documentRect, writer)
                document.add(image)
                document.newPage()
            }
            Log.v("Stage 8", "Image adding")
            document.close()
            Log.v("Stage 7", "Document Closed$mPath")
            Log.v("Stage 8", "Record inserted in database")
        } catch (e: Exception) {
            e.printStackTrace()
            mSuccess = false
        }
    }

    private fun addPageNumber(documentRect: Rectangle, writer: PdfWriter) {
        if (mPageNumStyle != null) {
            ColumnText.showTextAligned(
                writer.directContent,
                Element.ALIGN_BOTTOM,
                getPhrase(writer, mPageNumStyle, mImagesUri.size),
                (documentRect.right + documentRect.left) / 2,
                documentRect.bottom + 25, 0f
            )
        }
    }

    private fun getPhrase(writer: PdfWriter, pageNumStyle: String, size: Int): Phrase {
        val phrase: Phrase = when (pageNumStyle) {
            PDFConstants.PG_NUM_STYLE_PAGE_X_OF_N -> Phrase(String.format("Page %d of %d", writer.pageNumber, size))
            PDFConstants.PG_NUM_STYLE_X_OF_N -> Phrase(String.format("%d of %d", writer.pageNumber, size))
            else -> Phrase(String.format("%d", writer.pageNumber))
        }
        return phrase
    }

    private fun onPostExecute() {
        mOnPDFCreatedInterface.onPDFCreated(mSuccess, mPath, mFileName)
    }

    private fun getBaseColor(color: Int): BaseColor {
        return BaseColor(
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
    }
}
