package com.alfresco.content.process.ui.models

import com.alfresco.content.data.Entry

object DataHolder {

    val contentList: MutableList<Entry> = mutableListOf()

    fun observeUploads(observerId: String) {
    }
}
