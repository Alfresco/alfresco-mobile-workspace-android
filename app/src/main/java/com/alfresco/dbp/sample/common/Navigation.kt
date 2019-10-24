package com.alfresco.dbp.sample.common

import android.app.Activity
import android.net.Uri
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

/**
 *
 * Created by Bogdan Roatis on 24 Oct 2019.
 */

private const val NAVIGATION_BASE_PATH = "alfresco.sample://"

enum class Screens(val uriPath: String) {
    REFRESH("${NAVIGATION_BASE_PATH}refreshToken"),
}

fun Fragment.navigateToRefreshToken() {
    navigate(Screens.REFRESH)
}

fun Activity.navigate(@IdRes viewId: Int, screen: Screens) {
    val uri = Uri.parse(screen.uriPath)
    findNavController(viewId).navigate(uri)
}

fun Fragment.navigate(screen: Screens) {
    val uri = Uri.parse(screen.uriPath)
    findNavController().navigate(uri)
}
