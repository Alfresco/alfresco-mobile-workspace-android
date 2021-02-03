package com.alfresco.content

import android.net.Uri
import androidx.navigation.NavController
import com.alfresco.content.data.Entry

fun NavController.navigateTo(entry: Entry) {
    when (entry.type) {
        Entry.Type.File -> navigateToFile(entry)
        Entry.Type.Folder -> navigateToFolder(entry)
        Entry.Type.Site -> navigateToSite(entry)
        Entry.Type.FileLink -> navigateFileLink(entry)
        Entry.Type.FolderLink -> navigateFolderLink(entry)
        else -> { } // no-op for now
    }
}

private fun NavController.navigateToFolder(entry: Entry) {
    val domain =
        if (entry.hasOfflineStatus) {
            "offline"
        } else {
            "content"
        }
    navigate(Uri.parse("alfresco://${domain}/folder/${entry.id}?title=${Uri.encode(entry.title)}"))
}

private fun NavController.navigateToSite(entry: Entry) {
    navigate(Uri.parse("alfresco://content/site/${entry.id}?title=${Uri.encode(entry.title)}"))
}

private fun NavController.navigateToFile(entry: Entry) {
    navigate(Uri.parse("alfresco://content/file/${entry.id}/preview?title=${Uri.encode(entry.title)}&type=${entry.mimeType}"))
}

private fun NavController.navigateFileLink(entry: Entry) {
    navigate(Uri.parse("alfresco://content/file/${entry.otherId}/preview?title=${Uri.encode(entry.title)}&type=${entry.mimeType}"))
}

private fun NavController.navigateFolderLink(entry: Entry) {
    navigate(Uri.parse("alfresco://content/folder/${entry.otherId}?title=${Uri.encode(entry.title)}"))
}
