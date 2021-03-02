package com.alfresco.content

import android.net.Uri
import androidx.navigation.NavController
import com.alfresco.content.data.Entry

fun NavController.navigateTo(entry: Entry) {
    when (entry.type) {
        Entry.Type.FILE -> navigateToFile(entry)
        Entry.Type.FOLDER -> navigateToFolder(entry)
        Entry.Type.SITE -> navigateToSite(entry)
        Entry.Type.FILE_LINK -> navigateFileLink(entry)
        Entry.Type.FOLDER_LINK -> navigateFolderLink(entry)
        else -> { } // no-op for now
    }
}

private fun NavController.navigateToFolder(entry: Entry) =
    navigateToFolder(entry.id, entry.name, modeFor(entry))

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
    navigate(Uri.parse("$BASE_URI/browse/site/remote/${entry.id}?title=${Uri.encode(entry.name)}"))

private fun NavController.navigateToFile(entry: Entry) =
    navigate(Uri.parse("$BASE_URI/view/${modeFor(entry)}/${entry.id}/preview?title=${Uri.encode(entry.name)}"))

private fun NavController.navigateFileLink(entry: Entry) =
    navigate(Uri.parse("$BASE_URI/view/remote/${entry.otherId}/preview?title=${Uri.encode(entry.name)}"))

private fun NavController.navigateFolderLink(entry: Entry) =
    navigate(Uri.parse("$BASE_URI/browse/folder/remote/${entry.otherId}?title=${Uri.encode(entry.name)}"))

fun NavController.navigateToContextualSearch(id: String, title: String) =
    navigate(Uri.parse("$BASE_URI/search/folder/$id?title=${Uri.encode(title)}"))

private const val BASE_URI = "alfresco://content"
