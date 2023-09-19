package com.alfresco.content.move

import android.content.Context
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        println("MoveFragment.onAttach")
    }

    override fun onStart() {
        super.onStart()
        println("MoveFragment.onStart")
        val nodeId = viewModel.getMyFilesNodeId()
        args.entryObj?.let {
            findNavController().navigateToMoveParent(nodeId, it.id, getString(R.string.browse_menu_personal))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("MoveFragment.onCreate")
        args = MoveArgs.with(requireArguments())



    }




    override fun invalidate() = withState(viewModel) { state ->
    }
}
