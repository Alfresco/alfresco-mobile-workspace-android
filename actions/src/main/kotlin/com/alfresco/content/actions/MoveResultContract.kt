package com.alfresco.content.actions

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper
import com.alfresco.content.data.Entry

/**
 * Mark as MoveResultContract
 */
class MoveResultContract(private val entryObj: Entry?) : ActivityResultContract<Unit, String?>() {
    @CallSuper
    override fun createIntent(context: Context, input: Unit): Intent {
        intent.putExtra(ENTRY_OBJ_KEY, entryObj)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        return if (intent == null || resultCode != Activity.RESULT_OK) {
            null
        } else {
            intent.extras?.getString(OUTPUT_KEY)
        }
    }

    companion object {
        const val OUTPUT_KEY = "targetParentId"
        const val ENTRY_OBJ_KEY = "entryObj"
        const val MOVE_ID_KEY = "moveId"
        lateinit var intent: Intent

        /**
         * adding intent for MoveActivity
         */
        fun addMoveIntent(moveIntent: Intent) {
            intent = moveIntent
        }
    }
}
