package com.alfresco.content.viewer.common

import androidx.annotation.LayoutRes
import com.airbnb.mvrx.BaseMvRxFragment

interface LoadingListener {
    fun onContentLoaded()
    fun onContentError()
}

abstract class ChildViewerFragment(@LayoutRes contentLayoutId: Int = 0) :
    BaseMvRxFragment(contentLayoutId) {
    var loadingListener: LoadingListener? = null

    open fun showInfoWhenLoaded(): Boolean = false
}
