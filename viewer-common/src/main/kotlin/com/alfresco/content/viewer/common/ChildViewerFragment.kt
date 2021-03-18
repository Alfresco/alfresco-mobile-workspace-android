package com.alfresco.content.viewer.common

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

interface LoadingListener {
    fun onContentLoaded()
    fun onContentError()
}

abstract class ChildViewerFragment(@LayoutRes contentLayoutId: Int = 0) :
    Fragment(contentLayoutId) {
    var loadingListener: LoadingListener? = null

    open fun showInfoWhenLoaded(): Boolean = false
}
