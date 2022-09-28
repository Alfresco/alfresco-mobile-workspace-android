package com.alfresco.content.component

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.Mavericks

internal typealias SearchUserComponentApplyCallback = (String, String, Map<String, String>) -> Unit
internal typealias SearchUserComponentResetCallback = (String, String, Map<String, String>) -> Unit
internal typealias SearchUserComponentCancelCallback = () -> Unit

/**
 * Builder for build the search user component sheet
 */
data class SearchUserComponentBuilder(
    val context: Context,
    val componentData: ComponentData,
    var onApply: SearchUserComponentApplyCallback? = null,
    var onReset: SearchUserComponentResetCallback? = null,
    var onCancel: SearchUserComponentCancelCallback? = null
) {

    /**
     * Filter sheet apply callback
     */
    fun onApply(callback: SearchUserComponentApplyCallback?) =
        apply { this.onApply = callback }

    /**
     * Filter sheet reset callback
     */
    fun onReset(callback: SearchUserComponentResetCallback?) =
        apply { this.onReset = callback }

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
            arguments = bundleOf(Mavericks.KEY_ARG to componentData)
            onApply = this@SearchUserComponentBuilder.onApply
            onReset = this@SearchUserComponentBuilder.onReset
            onCancel = this@SearchUserComponentBuilder.onCancel
        }.show(fragmentManager, SearchUserComponentSheet::class.java.simpleName)
    }
}
