package com.alfresco.content.component.searchusergroup

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.Mavericks
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.UserGroupDetails

internal typealias SearchUserComponentApplyCallback = (UserGroupDetails) -> Unit
internal typealias SearchUserComponentCancelCallback = () -> Unit

/**
 * Builder for build the search user and group component sheet
 */
data class SearchUserGroupComponentBuilder(
    val context: Context,
    val parentEntry: ParentEntry,
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

        SearchUserGroupComponentSheet().apply {
            arguments = bundleOf(Mavericks.KEY_ARG to parentEntry)
            onApply = this@SearchUserGroupComponentBuilder.onApply
            onCancel = this@SearchUserGroupComponentBuilder.onCancel
        }.show(fragmentManager, SearchUserGroupComponentSheet::class.java.simpleName)
    }
}
