package com.alfresco.content.browse.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentBrowseMenuBinding
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.PageView
import com.alfresco.content.navigateToFolder
import com.alfresco.content.navigateToKnownPath
import com.alfresco.content.viewer.ViewerArgs.Companion.ID_KEY
import com.alfresco.content.viewer.ViewerArgs.Companion.MODE_KEY

class BrowseMenuFragment : Fragment(), MavericksView {

    private val viewModel: BrowseMenuViewModel by fragmentViewModel()
    private lateinit var binding: FragmentBrowseMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBrowseMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().intent?.let {
            it.extras?.let { bundle ->
                if (!bundle.getString(MODE_KEY).isNullOrEmpty()) {
                    navigateTo(
                        getString(R.string.nav_path_my_files), "", PageView.PersonalFiles,
                        bundle.getString(ID_KEY, "")
                    )
                    it.removeExtra(ID_KEY)
                    it.removeExtra(MODE_KEY)
                }
            }
        }
    }

    override fun invalidate() = withState(viewModel) {
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

    override fun onResume() {
        super.onResume()
        AnalyticsManager().screenViewEvent(PageView.Browse)
    }

    private fun navigateTo(path: String, title: String, pageView: PageView, nodeId: String = viewModel.getMyFilesNodeId()) {
        AnalyticsManager().screenViewEvent(pageView)
        if (path == getString(R.string.nav_path_my_files)) {
            findNavController().navigateToFolder(nodeId, title)
        } else {
            findNavController().navigateToKnownPath(path, title)
        }
    }
}
