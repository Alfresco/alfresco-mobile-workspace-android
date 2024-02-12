package com.alfresco.content.process.ui.components

import androidx.compose.ui.text.TextLayoutResult

class TextLayoutHandler(
    private val minimumLineLength: Int,
    private val onEllipsisChanged: (Boolean) -> Unit
) {
    fun handleTextLayout(textLayoutResult: TextLayoutResult) {
        if (textLayoutResult.lineCount > minimumLineLength - 1) {
            if (textLayoutResult.isLineEllipsized(minimumLineLength - 1)) {
                onEllipsisChanged(true)
            } else {
                onEllipsisChanged(false)
            }
        } else {
            onEllipsisChanged(false)
        }
    }
}