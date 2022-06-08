package com.zynksoftware.documentscanner.ui

import android.content.Context
import android.graphics.Bitmap
import com.zynksoftware.documentscanner.manager.SessionManager

object DocumentScanner {

    fun init(context: Context, configuration: Configuration = Configuration()) {
        System.loadLibrary("opencv_java4")
        val sessionManager = SessionManager(context)
        if (configuration.imageQuality in 1..100) {
            sessionManager.setImageQuality(configuration.imageQuality)
        }
        sessionManager.setImageSize(configuration.imageSize)
        sessionManager.setImageType(configuration.imageType)
    }

    data class Configuration(
        var imageQuality: Int = 100,
        var imageSize: Long = -1,
        var imageType: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    )
}
