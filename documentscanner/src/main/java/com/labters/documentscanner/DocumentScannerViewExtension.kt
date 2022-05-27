package com.labters.documentscanner

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import kotlinx.coroutines.flow.flow

/**
 * Scaled bitmap with new height and width
 */
fun scaledBitmap(bitmap: Bitmap, width: Int, height: Int) = flow<Bitmap> {
    val m = Matrix()
    m.setRectToRect(
        RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()), RectF(
            0f, 0f,
            width.toFloat(),
            height.toFloat()
        ), Matrix.ScaleToFit.CENTER
    )
    emit(Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true))
}

/**
 * picked the polygon view points from the image and returns as bitmap
 */
@Throws
fun DocumentScannerView.getCroppedImage(): Bitmap {
    val points: Map<Int, PointF> = polygonView.points
    val xRatio: Float = selectedImage.width.toFloat() / image.width
    val yRatio: Float = selectedImage.height.toFloat() / image.height
    val x1 = points[0]!!.x * xRatio
    val x2 = points[1]!!.x * xRatio
    val x3 = points[2]!!.x * xRatio
    val x4 = points[3]!!.x * xRatio
    val y1 = points[0]!!.y * yRatio
    val y2 = points[1]!!.y * yRatio
    val y3 = points[2]!!.y * yRatio
    val y4 = points[3]!!.y * yRatio
    val finalBitmap: Bitmap = selectedImage.copy(selectedImage.config, true)
    return nativeClass.getScannedBitmap(finalBitmap, x1, y1, x2, y2, x3, y3, x4, y4)
}
