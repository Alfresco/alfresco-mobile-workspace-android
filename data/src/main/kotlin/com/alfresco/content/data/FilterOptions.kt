package com.alfresco.content.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FilterOptions(
    val label: String = "",
    val query: String = "",
    val value: String = ""
) : Parcelable
