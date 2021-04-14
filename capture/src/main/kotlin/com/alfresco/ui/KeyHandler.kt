package com.alfresco.ui

import android.view.KeyEvent

interface KeyHandler {
    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean
}
