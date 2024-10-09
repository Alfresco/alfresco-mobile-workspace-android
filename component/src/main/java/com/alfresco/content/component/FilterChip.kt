package com.alfresco.content.component

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.chip.Chip

/**
 * Convenience subclass which enables toggling checked status without notifying the listener.
 */
class FilterChip(context: Context, attrs: AttributeSet?) : Chip(context, attrs) {
    private var onCheckedChangeListener: OnCheckedChangeListener? = null

    override fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        onCheckedChangeListener = listener
        super.setOnCheckedChangeListener(listener)
    }

    /**
     * Changes [checked] state and will [notify] the listener if required.
     */
    fun setChecked(
        checked: Boolean,
        notify: Boolean,
    ) {
        if (!notify) {
            super.setOnCheckedChangeListener(null)
            super.setChecked(checked)
            super.setOnCheckedChangeListener(onCheckedChangeListener)
        } else {
            super.setChecked(checked)
        }
    }

    /**
     * Toggles chip checked state and will [notify] the listener if required.
     */
    fun toggle(notify: Boolean) {
        if (!notify) {
            super.setOnCheckedChangeListener(null)
            super.toggle()
            super.setOnCheckedChangeListener(onCheckedChangeListener)
        } else {
            super.toggle()
        }
    }

    /**
     * Unchecks the current chip and will [notify] listener if required.
     */
    fun uncheck(notify: Boolean) {
        if (isChecked) {
            if (!notify) {
                super.setOnCheckedChangeListener(null)
                super.setChecked(false)
                super.setOnCheckedChangeListener(onCheckedChangeListener)
            } else {
                super.setChecked(false)
            }
        }
    }

    /**
     * Checks the current chip and will [notify] listener if required.
     */
    fun check(notify: Boolean) {
        if (!isChecked) {
            if (!notify) {
                super.setOnCheckedChangeListener(null)
                super.setChecked(true)
                super.setOnCheckedChangeListener(onCheckedChangeListener)
            } else {
                super.setChecked(true)
            }
        }
    }
}
