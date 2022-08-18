package com.alfresco.content.browse.tasks.attachments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentAttachedFilesBinding
import com.alfresco.content.browse.tasks.detail.TaskDetailViewModel
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.PageView
import com.alfresco.content.simpleController
import com.alfresco.ui.getDrawableForAttribute

/**
 * Marked as AttachedFilesFragment class
 */
class AttachedFilesFragment : Fragment(), MavericksView {

    val viewModel: TaskDetailViewModel by activityViewModel()
    private lateinit var binding: FragmentAttachedFilesBinding
    private val epoxyController: AsyncEpoxyController by lazy { epoxyController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAttachedFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AnalyticsManager().screenViewEvent(PageView.Comments)

        binding.toolbar.apply {
            navigationContentDescription = getString(R.string.label_navigation_back)
            navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
            title = resources.getString(R.string.title_attached_files)
        }

        binding.recyclerView.setController(epoxyController)

        epoxyController.adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    // @see: https://github.com/airbnb/epoxy/issues/224
                    binding.recyclerView.layoutManager?.scrollToPosition(0)
                }
            }
        })
        binding.refreshLayout.setOnRefreshListener {
            viewModel.getComments()
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        if (state.requestContents.complete) {
            binding.refreshLayout.isRefreshing = false
        }

        epoxyController.requestModelBuild()

        if (state.listContents.size > 4) {
            binding.tvNoOfAttachments.visibility = View.VISIBLE
            binding.tvNoOfAttachments.text = getString(R.string.text_multiple_attachment, state.listContents.size)
        } else {
            binding.tvNoOfAttachments.visibility = View.GONE
        }
    }

    private fun epoxyController() = simpleController(viewModel) { state ->

        if (state.listContents.isNotEmpty()) {
            state.listContents.forEach { obj ->
                listViewAttachmentRow {
                    id(obj.id)
                    data(obj)
                }
            }
            binding.recyclerView.post {
                binding.recyclerView.scrollToPosition(state.listContents.size - 1)
            }
        }
    }
}
