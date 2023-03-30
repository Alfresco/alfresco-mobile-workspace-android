package com.alfresco.content.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Marked as FilterOptions class
 */
@Parcelize
data class FilterOptions(
    val label: String = "",
    val query: String = "",
    val value: String = "",
    val default: Boolean = false
) : Parcelable
