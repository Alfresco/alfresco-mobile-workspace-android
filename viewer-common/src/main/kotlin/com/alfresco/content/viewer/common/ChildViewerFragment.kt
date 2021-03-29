package com.alfresco.content.viewer.common

import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

interface LoadingListener {
    fun onContentLoaded()
    fun onContentError()
}

abstract class ChildViewerFragment(@LayoutRes contentLayoutId: Int = 0) :
    Fragment(contentLayoutId) {
    var loadingListener: LoadingListener? = null
    var onClickListener: View.OnClickListener? = null
    var onControlsVisibilityChange: ((visibility: Int) -> Unit)? = null

    open fun showInfoWhenLoaded(): Boolean = false
}
