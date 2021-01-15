package com.alfresco.content.viewer.common

import androidx.annotation.LayoutRes
import com.airbnb.mvrx.BaseMvRxFragment
import java.lang.ref.WeakReference

interface LoadingListener {
    fun onContentLoaded()
    fun onContentError()
}

abstract class ChildViewerFragment(@LayoutRes contentLayoutId: Int = 0) :
    BaseMvRxFragment(contentLayoutId) {
    protected var loadingListener: WeakReference<LoadingListener> = WeakReference(null)

    fun setLoadingListener(listener: LoadingListener) {
        loadingListener = WeakReference(listener)
    }

    open fun showInfoWhenLoaded(): Boolean = false
}
