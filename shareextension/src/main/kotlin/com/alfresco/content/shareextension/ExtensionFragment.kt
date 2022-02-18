package com.alfresco.content.shareextension

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.navigateToExtension
import kotlinx.parcelize.Parcelize

/**
 * Mark as ExtensionArgs
 */
@Parcelize
data class ExtensionArgs(
    val path: String
) : Parcelable {
    companion object {
        private const val PATH_KEY = "path"

        /**
         * return the ExtensionArgs obj
         */
        fun with(args: Bundle): ExtensionArgs {
            return ExtensionArgs(
                args.getString(PATH_KEY, "")
            )
        }
    }
}

/**
 * Mark as ExtensionFragment
 */
class ExtensionFragment : Fragment(), MavericksView {
    private lateinit var args: ExtensionArgs
    val viewModel: ExtensionViewModel by fragmentViewModelWithArgs { args }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = ExtensionArgs.with(requireArguments())

        val nodeId = viewModel.getMyFilesNodeId()
        findNavController().navigateToExtension(nodeId, "")
    }

    override fun invalidate() = withState(viewModel) { state ->
    }
}
