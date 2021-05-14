package com.alfresco.capture

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CaptureItem(
    val uri: Uri,
    val name: String,
    val description: String,
    val mimeType: String
) : Parcelable
