package com.alfresco.content.move

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.content.data.Entry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.navigateToMoveParent
import kotlinx.parcelize.Parcelize

/**
 * Mark as MoveArgs
 */
@Parcelize
data class MoveArgs(
    val path: String,
    val entryObj: Entry?,
) : Parcelable {
    companion object {
        private const val PATH_KEY = "path"
        private const val ENTRY_OBJ_KEY = "entryObj"

        /**
         * return the MoveArgs obj
         */
        fun with(args: Bundle): MoveArgs {
            return MoveArgs(
                args.getString(PATH_KEY, ""),
                args.getParcelable(ENTRY_OBJ_KEY),
            )
        }
    }
}

/**
 * Mark as MoveFragment
 */
class MoveFragment : Fragment(), MavericksView {
    private lateinit var args: MoveArgs

    @OptIn(InternalMavericksApi::class)
    val viewModel: MoveViewModel by fragmentViewModelWithArgs { args }

    override fun onStart() {
        super.onStart()
        val nodeId = viewModel.getMyFilesNodeId()

        val entryObj = args.entryObj
        if (args.entryObj != null) {
            entryObj?.id?.let { findNavController().navigateToMoveParent(nodeId, it, getString(R.string.browse_menu_personal)) }
        } else {
            findNavController().navigateToMoveParent(nodeId, "", getString(R.string.browse_menu_personal), true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args = MoveArgs.with(requireArguments())
    }

    override fun invalidate() =
        withState(viewModel) { state ->
        }
}
