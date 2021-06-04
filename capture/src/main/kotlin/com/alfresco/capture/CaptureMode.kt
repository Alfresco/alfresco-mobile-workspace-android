package com.alfresco.capture

import android.content.Context

enum class CaptureMode {
    Photo,
    Video;

    fun title(context: Context) =
        when(this) {
            Photo -> context.getString(R.string.capture_mode_photo)
            Video -> context.getString(R.string.capture_mode_video)
        }
}