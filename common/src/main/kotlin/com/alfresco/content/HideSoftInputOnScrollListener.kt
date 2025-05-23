package com.alfresco.content

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.RecyclerView

class HideSoftInputOnScrollListener : RecyclerView.OnScrollListener() {
    private var imm: InputMethodManager? = null

    override fun onScrollStateChanged(
        recyclerView: RecyclerView,
        newState: Int,
    ) {
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING && recyclerView.childCount > 0) {
            if (imm == null) {
                imm = recyclerView.context.getSystemService()
            }
            if (imm?.isAcceptingText == true) {
                val activity: Activity? = recyclerView.context.findBaseContext()
                val currentFocus = activity?.currentFocus
                if (currentFocus != null) {
                    imm?.hideSoftInputFromWindow(currentFocus.windowToken, 0)
                }
            }
        }
    }
}
