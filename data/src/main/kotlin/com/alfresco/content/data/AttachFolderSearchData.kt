package com.alfresco.content.data

import com.alfresco.content.data.payloads.FieldsData

data class AttachFolderSearchData(val entry: Entry? = null)
data class AttachFilesData(val field: FieldsData? = null, val deletedFiles: MutableMap<String, Entry>)
