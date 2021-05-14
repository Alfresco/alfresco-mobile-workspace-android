package com.alfresco.capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper

/**
 * An [ActivityResultContract] to [take a picture] and returns a-[CaptureItem].
 */
class CapturePhotoResultContract : ActivityResultContract<Unit, CaptureItem?>() {
    @CallSuper
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(context, CaptureActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): CaptureItem? {
        return if (intent == null || resultCode != Activity.RESULT_OK) null
        else intent.extras?.getParcelable(OUTPUT_KEY)
    }

    internal companion object {
        const val OUTPUT_KEY = "item"
    }
}
