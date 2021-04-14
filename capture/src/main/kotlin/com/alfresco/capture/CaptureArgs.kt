package com.alfresco.capture

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import com.airbnb.mvrx.Mavericks
import kotlinx.parcelize.Parcelize

@Parcelize
data class CaptureArgs(
    val parentId: String
) : Parcelable {
    companion object {
        private const val PARENT_ID_KEY = "parentId"

        fun with(args: Bundle): CaptureArgs {
            return CaptureArgs(
                args.getString(PARENT_ID_KEY, "")
            )
        }

        fun makeArguments(parentId: String) =
            bundleOf(Mavericks.KEY_ARG to CaptureArgs(parentId))
    }
}
