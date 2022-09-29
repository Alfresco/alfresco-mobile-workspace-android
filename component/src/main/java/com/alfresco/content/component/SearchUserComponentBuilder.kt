package com.alfresco.content.component

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.Mavericks
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.data.UserDetails

internal typealias SearchUserComponentApplyCallback = (UserDetails) -> Unit
internal typealias SearchUserComponentCancelCallback = () -> Unit

/**
 * Builder for build the search user component sheet
 */
data class SearchUserComponentBuilder(
    val context: Context,
    val taskEntry: TaskEntry,
    var onApply: SearchUserComponentApplyCallback? = null,
    var onCancel: SearchUserComponentCancelCallback? = null
) {

    /**
     * Filter sheet apply callback
     */
    fun onApply(callback: SearchUserComponentApplyCallback?) =
        apply { this.onApply = callback }

    /**
     * Filter sheet cancel callback
     */
    fun onCancel(callback: SearchUserComponentCancelCallback?) =
        apply { this.onCancel = callback }

    /**
     * Filter sheet show method
     */
    fun show() {

        val fragmentManager = when (context) {
            is AppCompatActivity -> context.supportFragmentManager
            is Fragment -> context.childFragmentManager
            else -> throw IllegalArgumentException()
        }

        SearchUserComponentSheet().apply {
            arguments = bundleOf(Mavericks.KEY_ARG to taskEntry)
            onApply = this@SearchUserComponentBuilder.onApply
            onCancel = this@SearchUserComponentBuilder.onCancel
        }.show(fragmentManager, SearchUserComponentSheet::class.java.simpleName)
    }
}
