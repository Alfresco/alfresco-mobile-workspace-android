package com.alfresco.dbp.sample.common

import android.view.View

/**
 * Contains extension methods for [View]
 *
 * Created by Bogdan Roatis on 24 August 2019.
 */

fun View.show(show: Boolean) {
    if (show) {
        show()
    } else {
        hide()
    }
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}
