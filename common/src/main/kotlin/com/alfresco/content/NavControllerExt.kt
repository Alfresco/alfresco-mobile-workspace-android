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
        else -> {
        } // no-op for now
    }
}

private fun NavController.navigateToFolder(entry: Entry) {
    if (entry.isExtension)
        navigateToChildFolder(entry.id, entry.name, modeFor(entry))
    else navigateToFolder(entry.id, entry.name, modeFor(entry))
}

private fun modeFor(entry: Entry) =
    if (entry.hasOfflineStatus) {
        "local"
    } else {
        REMOTE
    }

fun NavController.navigateToFolder(id: String, title: String, mode: String = REMOTE) {
    navigate(Uri.parse("$BASE_URI/browse/folder/$mode/$id?title=${Uri.encode(title)}"))
}

fun NavController.navigateToKnownPath(path: String, title: String) =
    navigate(Uri.parse("$BASE_URI/browse/$path/$REMOTE?title=${Uri.encode(title)}"))

private fun NavController.navigateToSite(entry: Entry) =
    navigate(Uri.parse("$BASE_URI/browse/site/$REMOTE/${entry.id}?title=${Uri.encode(entry.name)}"))

private fun NavController.navigateToFile(entry: Entry) =
    navigate(Uri.parse("$BASE_URI/view/${modeFor(entry)}/${entry.id}/preview?title=${Uri.encode(entry.name)}"))

private fun NavController.navigateFileLink(entry: Entry) =
    navigate(Uri.parse("$BASE_URI/view/$REMOTE/${entry.otherId}/preview?title=${Uri.encode(entry.name)}"))

private fun NavController.navigateFolderLink(entry: Entry) =
    navigate(Uri.parse("$BASE_URI/browse/folder/$REMOTE/${entry.otherId}?title=${Uri.encode(entry.name)}"))

fun NavController.navigateToContextualSearch(id: String, title: String, isExtension: Boolean) =
    navigate(Uri.parse("$BASE_URI/search/folder/$id/$isExtension?title=${Uri.encode(title)}"))

/**
 * navigate to browse parent folder
 */
fun NavController.navigateToParent(id: String, title: String, mode: String = REMOTE) {
    navigate(Uri.parse("$BASE_URI/browse_parent/extension/$mode/$id?title=${Uri.encode(title)}"))
}

/**
 * navigate to browse child folder
 */
fun NavController.navigateToChildFolder(id: String, title: String, mode: String = REMOTE) {
    navigate(Uri.parse("$BASE_URI/browse_child/extension/$mode/$id?title=${Uri.encode(title)}"))
}

/**
 * navigate to transfer files
 */
fun NavController.navigateToUploadFilesPath(extension: Boolean, title: String) =
    navigate(Uri.parse("$BASE_URI/transfer_files/$extension?title=${Uri.encode(title)}"))

/**
 * navigate to preview activity using deep linking
 */
fun NavController.navigateToPreview(mimeType: String, path: String, title: String) =
    navigate(Uri.parse("$BASE_URI/view/preview?title=${Uri.encode(title)},mimeType=$mimeType,path=$path"))

private const val BASE_URI = "alfresco://content"
private const val REMOTE = "remote"
