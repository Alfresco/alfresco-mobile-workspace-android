package com.alfresco.scan

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper

/**
 * An [ActivityResultContract] to [take a picture] and returns a-[ScanItem].
 */
class ScanResultContract : ActivityResultContract<Unit, List<ScanItem>?>() {
    @CallSuper
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(context, ScanActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<ScanItem>? {
        return if (intent == null || resultCode != Activity.RESULT_OK) null
        else intent.extras?.getParcelableArrayList(OUTPUT_KEY)
    }

    internal companion object {
        const val OUTPUT_KEY = "item"
        const val SCAN_KEY = "isScan"
    }
}
