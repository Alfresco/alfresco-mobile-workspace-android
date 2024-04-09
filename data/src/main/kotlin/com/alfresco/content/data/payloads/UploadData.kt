package com.alfresco.content.data.payloads

import android.os.Parcelable
import com.alfresco.content.data.ProcessEntry
import kotlinx.parcelize.Parcelize

@Parcelize
data class UploadData(
    val field: FieldsData = FieldsData(),
    val process: ProcessEntry = ProcessEntry(),
) : Parcelable
