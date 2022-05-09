package com.alfresco.content.move

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.navigateToMoveParent
import kotlinx.parcelize.Parcelize

/**
 * Mark as MoveArgs
 */
@Parcelize
data class MoveArgs(
    val path: String
) : Parcelable {
    companion object {
        private const val PATH_KEY = "path"

        /**
         * return the MoveArgs obj
         */
        fun with(args: Bundle): MoveArgs {
            return MoveArgs(
                args.getString(PATH_KEY, "")
            )
        }
    }
}

/**
 * Mark as MoveFragment
 */
class MoveFragment : Fragment(), MavericksView {
    private lateinit var args: MoveArgs
    val viewModel: MoveViewModel by fragmentViewModelWithArgs { args }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = MoveArgs.with(requireArguments())

        val nodeId = viewModel.getMyFilesNodeId()

        findNavController().navigateToMoveParent(nodeId, "Personal Files")
    }

    override fun invalidate() = withState(viewModel) { state ->
    }
}
