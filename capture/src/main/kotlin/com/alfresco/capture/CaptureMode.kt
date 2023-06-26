package com.alfresco.capture

import android.content.Context

enum class CaptureMode {
    Photo,
    Video,
    ;

    fun title(context: Context) =
        when (this) {
            Photo -> context.getString(R.string.capture_mode_photo)
            Video -> context.getString(R.string.capture_mode_video)
        }

    fun aspectRatio() =
        when (this) {
            Photo -> RATIO_4_3
            Video -> RATIO_16_9
        }

    companion object {
        const val RATIO_4_3 = 4.0 / 3.0
        const val RATIO_16_9 = 16.0 / 9.0
    }
}
