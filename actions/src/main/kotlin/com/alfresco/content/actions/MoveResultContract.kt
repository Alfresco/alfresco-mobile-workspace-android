package com.alfresco.content.actions

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper

class MoveResultContract : ActivityResultContract<Unit, String?>() {
    @CallSuper
    override fun createIntent(context: Context, input: Unit): Intent {
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        return if (intent == null || resultCode != Activity.RESULT_OK) null
        else intent.extras?.getString(OUTPUT_KEY)
    }

    companion object {
        const val OUTPUT_KEY = "targetParentId"
        lateinit var intent: Intent
        fun addMoveIntent(moveIntent: Intent) {
            intent = moveIntent
        }
    }
}
