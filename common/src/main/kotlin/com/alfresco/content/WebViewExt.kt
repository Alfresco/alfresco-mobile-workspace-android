package com.alfresco.content

import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

object WebViewClickSupportListener : View.OnTouchListener {
    private var isPressed: Boolean = false
    private var isDragging: Boolean = false
    private var pressedTime: Long = 0L

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                pressedTime = event.eventTime
            }
            MotionEvent.ACTION_UP -> {
                val delay = event.eventTime - pressedTime
                if (isPressed && !isDragging &&
                    delay >= ViewConfiguration.getTapTimeout() &&
                    delay < ViewConfiguration.getLongPressTimeout()) {
                    view.performClick()
                }
                isPressed = false
                isDragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                isDragging = true
            }
            MotionEvent.ACTION_CANCEL -> {
                isPressed = false
                isDragging = false
            }
        }
        return false
    }
}
