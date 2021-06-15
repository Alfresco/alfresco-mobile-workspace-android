package com.alfresco.capture

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.Chronometer

class CaptureDurationView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
): Chronometer(
    ContextThemeWrapper(context, R.style.Widget_Alfresco_Camera_Mode_Button),
    attrs,
    defStyleAttr
) {

    constructor(context: Context) :
        this(context, null)

    constructor(context: Context, attrs: AttributeSet?) :
        this(context, attrs, 0)


    init {
        gravity = Gravity.CENTER
        val pad = resources.getDimension(R.dimen.capture_button_padding).toInt()
        setPadding(pad, 0, pad, 0)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)

        if (visibility == View.VISIBLE) {
            base = SystemClock.elapsedRealtime()
            isActivated = true
            start()
        } else {
            stop()
        }
    }
}