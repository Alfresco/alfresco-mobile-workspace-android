package com.alfresco.content

import android.net.Uri
import androidx.navigation.NavController
import com.alfresco.content.data.Entry

fun NavController.navigateTo(entry: Entry) {
    when (entry.type) {
        Entry.Type.File -> navigateToFile(entry)
        Entry.Type.Folder -> navigateToFolder(entry)
        Entry.Type.Site -> navigateToSite(entry)
        Entry.Type.FileLink -> navigateToFile(entry)
        Entry.Type.FolderLink -> navigateToFolder(entry)
        else -> { } // no-op for now
    }
}

private fun NavController.navigateToFolder(entry: Entry) {
    navigate(Uri.parse("alfresco://content/folder/${entry.id}?title=${Uri.encode(entry.title)}"))
}

private fun NavController.navigateToSite(entry: Entry) {
    navigate(Uri.parse("alfresco://content/site/${entry.id}?title=${Uri.encode(entry.title)}"))
}

private fun NavController.navigateToFile(entry: Entry) {
    navigate(Uri.parse("alfresco://content/file/${entry.id}/preview?title=${Uri.encode(entry.title)}&type=${entry.mimeType}"))
}
