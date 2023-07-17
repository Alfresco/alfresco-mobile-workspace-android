package com.alfresco.content.viewer.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChildViewerArgs(
    val uri: String,
    val type: String,
) : Parcelable
