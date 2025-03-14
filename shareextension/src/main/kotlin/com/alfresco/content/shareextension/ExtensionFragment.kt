package com.alfresco.content.shareextension

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentBrowseMenuBinding
import com.alfresco.content.browse.menu.browseMenuRow
import com.alfresco.content.browse.menu.browseMenuSeparator
import com.alfresco.content.common.SharedURLParser.Companion.ID_KEY
import com.alfresco.content.common.SharedURLParser.Companion.MODE_KEY
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.PageView
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.navigateToExtensionKnownPath
import com.alfresco.content.navigateToKnownPath
import com.alfresco.content.navigateToParent
import kotlinx.parcelize.Parcelize

/**
 * Mark as ExtensionArgs
 */
@Parcelize
data class ExtensionArgs(
    val path: String,
) : Parcelable {
    companion object {
        private const val PATH_KEY = "path"

        /**
         * return the ExtensionArgs obj
         */
        fun with(args: Bundle): ExtensionArgs {
            return ExtensionArgs(
                args.getString(PATH_KEY, ""),
            )
        }
    }
}

/**
 * Mark as ExtensionFragment
 */
class ExtensionFragment : Fragment(), MavericksView {
    private lateinit var args: ExtensionArgs
    private lateinit var binding: FragmentBrowseMenuBinding

    @OptIn(InternalMavericksApi::class)
    val viewModel: ExtensionViewModel by fragmentViewModelWithArgs { args }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = ExtensionArgs.with(requireArguments())
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBrowseMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().intent?.let {
            it.extras?.let { bundle ->
                if (!bundle.getString(MODE_KEY).isNullOrEmpty()) {
                    navigateTo(
                        getString(R.string.nav_path_my_files),
                        "",
                        PageView.PersonalFiles,
                        bundle.getString(ID_KEY, ""),
                    )
                    it.removeExtra(ID_KEY)
                    it.removeExtra(MODE_KEY)
                }
            }
        }

    }

    override fun invalidate() =
        withState(viewModel) {
            binding.recyclerView.withModels {
                it.entries.forEach {
                    if (it.path.isNotEmpty()) {
                        browseMenuRow {
                            id(it.title)
                            entry(it)
                            clickListener { _ -> navigateTo(it.path, it.title, it.pageView) }
                        }
                    } else {
                        browseMenuSeparator {
                            id(it.title)
                        }
                    }
                }
            }
        }

    private fun navigateTo(
        path: String,
        title: String,
        pageView: PageView,
        nodeId: String = viewModel.getMyFilesNodeId(),
    ) {
        AnalyticsManager().screenViewEvent(pageView)
        if (path == getString(R.string.nav_path_my_files)) {
            findNavController().navigateToParent(nodeId, "")
        } else {
            findNavController().navigateToExtensionKnownPath(path, title)
        }
    }
}
