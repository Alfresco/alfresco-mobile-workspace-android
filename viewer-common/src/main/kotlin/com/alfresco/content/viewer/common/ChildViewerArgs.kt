package com.alfresco.content.viewer.common

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChildViewerArgs(
    val id: String,
    val uri: String,
    val type: String
) : Parcelable {
    companion object {
        private const val ID_KEY = "id"
        private const val URI_KEY = "uri"
        private const val TYPE_KEY = "type"

        fun with(args: Bundle): ChildViewerArgs {
            return ChildViewerArgs(
                args.getString(ID_KEY, ""),
                args.getString(URI_KEY, ""),
                args.getString(TYPE_KEY, "")
            )
        }
    }
}
