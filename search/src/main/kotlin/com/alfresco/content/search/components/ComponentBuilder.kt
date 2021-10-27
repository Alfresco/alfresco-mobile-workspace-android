package com.alfresco.content.search.components

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.Mavericks
import com.alfresco.content.search.SearchChipCategory

internal typealias ComponentApplyCallback = (String, String) -> Unit
internal typealias ComponentResetCallback = (String, String) -> Unit
internal typealias ComponentCancelCallback = () -> Unit

/**
     * Builder for build the component sheet
     */
    data class ComponentBuilder(
        val context: Context,
        val searchChipCategory: SearchChipCategory,
        var onApply: ComponentApplyCallback? = null,
        var onReset: ComponentResetCallback? = null,
        var onCancel: ComponentCancelCallback? = null
    ) {

        /**
         * Component sheet apply callback
         */
        fun onApply(callback: ComponentApplyCallback?) =
            apply { this.onApply = callback }

        /**
         * Component sheet reset callback
         */
        fun onReset(callback: ComponentResetCallback?) =
            apply { this.onReset = callback }

        /**
         * Component sheet cancel callback
         */
        fun onCancel(callback: ComponentCancelCallback?) =
            apply { this.onCancel = callback }

        /**
         * Component sheet show method
         */
        fun show() {
            val fragmentManager = when (context) {
                is AppCompatActivity -> context.supportFragmentManager
                is Fragment -> context.childFragmentManager
                else -> throw IllegalArgumentException()
            }
            CreateComponentsSheet().apply {
                arguments = bundleOf(Mavericks.KEY_ARG to searchChipCategory)
                onApply = this@ComponentBuilder.onApply
                onReset = this@ComponentBuilder.onReset
                onCancel = this@ComponentBuilder.onCancel
            }.show(fragmentManager, CreateComponentsSheet::class.java.simpleName)
        }
    }
