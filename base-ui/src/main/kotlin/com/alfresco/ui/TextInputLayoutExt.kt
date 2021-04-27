package com.alfresco.ui

import com.google.android.material.textfield.TextInputLayout

// Helper for setting text programmatically
var TextInputLayout.text: CharSequence
    get() = editText?.text ?: ""
    set(value) {
        // Change the text without hint animation
        val hintAnimationState = isHintAnimationEnabled
        isHintAnimationEnabled = false

        editText?.setText(value)

        // Restore hint animation state
        isHintAnimationEnabled = hintAnimationState

        // Changing text programmatically incorrectly displays clear text icon
        if (!isFocused && endIconMode == TextInputLayout.END_ICON_CLEAR_TEXT) {
            isEndIconVisible = false
        }
    }
