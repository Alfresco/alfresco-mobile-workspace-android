package com.alfresco.content.browse.tasks

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.Mavericks
import com.alfresco.content.data.TaskFilterData

internal typealias FilterApplyCallback = (String, String, Map<String, String>) -> Unit
internal typealias FilterResetCallback = (String, String, Map<String, String>) -> Unit
internal typealias FilterCancelCallback = () -> Unit

/**
 * Builder for build the task filter sheet
 */
data class TaskFilterBuilder(
    val context: Context,
    val taskFilterData: TaskFilterData,
    var onApply: FilterApplyCallback? = null,
    var onReset: FilterResetCallback? = null,
    var onCancel: FilterCancelCallback? = null
) {

    /**
     * Filter sheet apply callback
     */
    fun onApply(callback: FilterApplyCallback?) =
        apply { this.onApply = callback }

    /**
     * Filter sheet reset callback
     */
    fun onReset(callback: FilterResetCallback?) =
        apply { this.onReset = callback }

    /**
     * Filter sheet cancel callback
     */
    fun onCancel(callback: FilterCancelCallback?) =
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

        CreateFilterSheet().apply {
            arguments = bundleOf(Mavericks.KEY_ARG to taskFilterData)
            onApply = this@TaskFilterBuilder.onApply
            onReset = this@TaskFilterBuilder.onReset
            onCancel = this@TaskFilterBuilder.onCancel
        }.show(fragmentManager, CreateFilterSheet::class.java.simpleName)
    }
}
