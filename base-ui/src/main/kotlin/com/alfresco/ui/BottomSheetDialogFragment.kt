package com.alfresco.ui

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BottomSheetDialogFragment : BottomSheetDialogFragment() {
    open val requiresFullscreen = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val view = view ?: return

        // Fix for https://issuetracker.google.com/issues/37132390
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val bottomSheet =
                (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                BottomSheetBehavior.from<View>(it).apply {
                    val peekAmount = if (requiresFullscreen) 1.0 else PEEK_AMOUNT
                    peekHeight = ((it.parent as View).height * peekAmount).toInt()
                }
            }
        }
    }

    private companion object {
        const val PEEK_AMOUNT = 0.58
    }
}
