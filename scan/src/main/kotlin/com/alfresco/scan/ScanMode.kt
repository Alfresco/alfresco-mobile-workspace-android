package com.alfresco.scan

import android.content.Context

/**
 * Marked as ScanMode enum class
 */
enum class ScanMode {
    Photo;

    /**
     * set the title of current mode
     */
    fun title(context: Context) = context.getString(R.string.scan_mode_photo)

    /**
     * returns the aspect ratio for photo mode
     */
    fun aspectRatio() = RATIO_4_3

    companion object {
        const val RATIO_4_3 = 4.0 / 3.0
        const val RATIO_16_9 = 16.0 / 9.0
    }
}
