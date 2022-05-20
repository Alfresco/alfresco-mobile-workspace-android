package com.alfresco.ui

import android.view.KeyEvent

/**
 * Marked as ScanKeyHandler interface
 */
interface ScanKeyHandler {

    /**
     * returns true on key pressed events
     */
    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean
}
