package com.labters.documentscanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnAttach
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.labters.documentscanner.libraries.NativeClass
import com.labters.documentscanner.libraries.PolygonView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.opencv.core.MatOfPoint2f

/**
 * Marked as DocumentScannerView class
 */
class DocumentScannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var holder: FrameLayout
    lateinit var image: ImageView
    lateinit var polygonView: PolygonView

    lateinit var selectedImage: Bitmap
    private var isInitialized = false

    private var onLoad: OnLoadListener? = {
        Log.i(javaClass.simpleName, "loading = $it")
    }

    private val lifecycle: LifecycleOwner
        get() = findViewTreeLifecycleOwner()!!

    private val scope: LifecycleCoroutineScope
        get() = lifecycle.lifecycleScope

    val nativeClass = NativeClass()

    init {
        inflate(context, R.layout.document_scanner, this).run {
            doOnAttach {
                holder = findViewById(R.id.holder)
                image = findViewById(R.id.image)
                polygonView = findViewById(R.id.polygon_view)
                isInitialized = true
            }
        }
    }

    private fun initView() {
        scope.launch {
            onLoad?.invoke(true)
            setImageRotation()
            initializeCropping()
            onLoad?.invoke(false)
        }
    }

    /**
     * set bitmap on view for cropping
     */
    fun setImage(image: Bitmap) {
        selectedImage = image
        doWhenInitialised { initView() }
    }

    private suspend fun setImageRotation() {
        var tempBitmap = selectedImage.copy(selectedImage.config, true)
        for (i in 1..4) {
            val point2f = nativeClass.getPoint(tempBitmap)
            if (point2f == null) {
                tempBitmap = rotateBitmap(tempBitmap, (90 * i).toFloat()).first()
            } else {
                selectedImage = tempBitmap.copy(selectedImage.config, true)
                break
            }
        }
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Flow<Bitmap> = flow<Bitmap> {
        val matrix = Matrix()
        matrix.postRotate(angle)
        emit(Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true))
    }

    private suspend fun initializeCropping() {
        val scaledBitmap: Bitmap = scaledBitmap(
            selectedImage,
            holder.width,
            holder.height
        ).first()
        image.setImageBitmap(scaledBitmap)
        val tempBitmap = (image.drawable as BitmapDrawable).bitmap
        val pointFs = getEdgePoints(tempBitmap)
        polygonView.points = pointFs
        polygonView.visibility = VISIBLE
        val padding = resources.getDimension(R.dimen.scanPadding).toInt() * 2
        val layoutParams =
            LayoutParams(tempBitmap.width + padding, tempBitmap.height + padding)
        layoutParams.gravity = Gravity.CENTER
        polygonView.layoutParams = layoutParams
        polygonView.setPointColor(ContextCompat.getColor(context, R.color.blue))
    }

    private fun getEdgePoints(tempBitmap: Bitmap): Map<Int, PointF>? {
        val pointFs: List<PointF> = getContourEdgePoints(tempBitmap)
        return orderedValidEdgePoints(tempBitmap, pointFs)
    }

    private fun getContourEdgePoints(tempBitmap: Bitmap): List<PointF> {
        var point2f = nativeClass.getPoint(tempBitmap)
        if (point2f == null) point2f = MatOfPoint2f()
        val points = point2f.toArray().toList()
//        val points = listOf(*point2f.toArray())
        val result: MutableList<PointF> = ArrayList()
        for (i in points.indices) {
            result.add(PointF(points[i].x.toFloat(), points[i].y.toFloat()))
        }
        return result
    }

    private fun getOutlinePoints(tempBitmap: Bitmap): Map<Int, PointF> {
        val outlinePoints: MutableMap<Int, PointF> = HashMap()
        outlinePoints[0] = PointF(0f, 0f)
        outlinePoints[1] = PointF(tempBitmap.width.toFloat(), 0f)
        outlinePoints[2] = PointF(0f, tempBitmap.height.toFloat())
        outlinePoints[3] = PointF(tempBitmap.width.toFloat(), tempBitmap.height.toFloat())
        return outlinePoints
    }

    private fun orderedValidEdgePoints(
        tempBitmap: Bitmap,
        pointFs: List<PointF>
    ): Map<Int, PointF>? {
        var orderedPoints: Map<Int, PointF>? = polygonView.getOrderedPoints(pointFs)
        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(tempBitmap)
        }
        return orderedPoints
    }

    private fun doWhenInitialised(function: () -> Unit) {
        scope.launch {
            while (isInitialized.not()) {
                delay(500L)
            }
            function()
        }
    }
}
