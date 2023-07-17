package com.alfresco.content.component

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.Mavericks

internal typealias ComponentApplyCallback = (String, String, Map<String, String>) -> Unit
internal typealias ComponentResetCallback = (String, String, Map<String, String>) -> Unit
internal typealias ComponentCancelCallback = () -> Unit

/**
 * Builder for build the task filter sheet
 */
data class ComponentBuilder(
    val context: Context,
    val componentData: ComponentData,
    var onApply: ComponentApplyCallback? = null,
    var onReset: ComponentResetCallback? = null,
    var onCancel: ComponentCancelCallback? = null,
) {

    /**
     * Filter sheet apply callback
     */
    fun onApply(callback: ComponentApplyCallback?) =
        apply { this.onApply = callback }

    /**
     * Filter sheet reset callback
     */
    fun onReset(callback: ComponentResetCallback?) =
        apply { this.onReset = callback }

    /**
     * Filter sheet cancel callback
     */
    fun onCancel(callback: ComponentCancelCallback?) =
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

        ComponentSheet().apply {
            arguments = bundleOf(Mavericks.KEY_ARG to componentData)
            onApply = this@ComponentBuilder.onApply
            onReset = this@ComponentBuilder.onReset
            onCancel = this@ComponentBuilder.onCancel
        }.show(fragmentManager, ComponentSheet::class.java.simpleName)
    }
}
