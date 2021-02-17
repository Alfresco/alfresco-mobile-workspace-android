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

private fun NavController.navigateToFolder(entry: Entry) =
    navigateToFolder(entry.id, entry.title, modeFor(entry))

private fun modeFor(entry: Entry) =
    if (entry.hasOfflineStatus) {
        "local"
    } else {
        "remote"
    }

fun NavController.navigateToFolder(id: String, title: String, mode: String = "remote") {
    navigate(Uri.parse("$BASE_URI/browse/folder/$mode/$id?title=${Uri.encode(title)}"))
}

fun NavController.navigateToKnownPath(path: String, title: String) =
    navigate(Uri.parse("$BASE_URI/browse/$path/remote?title=${Uri.encode(title)}"))

private fun NavController.navigateToSite(entry: Entry) =
    navigate(Uri.parse("$BASE_URI/browse/site/remote/${entry.id}?title=${Uri.encode(entry.title)}"))

private fun NavController.navigateToFile(entry: Entry) =
    navigate(Uri.parse("$BASE_URI/view/${modeFor(entry)}/${entry.id}/preview?title=${Uri.encode(entry.title)}"))

private fun NavController.navigateFileLink(entry: Entry) =
    navigate(Uri.parse("$BASE_URI/view/remote/${entry.otherId}?title=${Uri.encode(entry.title)}"))

private fun NavController.navigateFolderLink(entry: Entry) =
    navigate(Uri.parse("$BASE_URI/browse/folder/${entry.otherId}?title=${Uri.encode(entry.title)}"))

private const val BASE_URI = "alfresco://content"
