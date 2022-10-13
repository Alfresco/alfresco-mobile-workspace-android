package com.alfresco.content.browse.tasks.attachments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.ActionOpenWith
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentAttachedFilesBinding
import com.alfresco.content.browse.preview.LocalPreviewActivity
import com.alfresco.content.browse.tasks.BaseDetailFragment
import com.alfresco.content.browse.tasks.detail.TaskDetailViewModel
import com.alfresco.content.browse.tasks.detail.TaskDetailViewState
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.Entry
import com.alfresco.content.data.PageView
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.listview.EntryListener
import com.alfresco.content.mimetype.MimeType
import com.alfresco.content.simpleController
import com.alfresco.ui.getDrawableForAttribute
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Marked as AttachedFilesFragment class
 */
class AttachedFilesFragment : BaseDetailFragment(), MavericksView, EntryListener {

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

        AnalyticsManager().screenViewEvent(PageView.AttachedFiles)

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
        viewModel.getContents()
        binding.refreshLayout.setOnRefreshListener {
            viewModel.getContents()
        }

        viewModel.setListener(this)
    }

    override fun onConfirmDelete(contentId: String) {
        viewModel.deleteAttachment(contentId)
    }

    override fun invalidate() = withState(viewModel) { state ->
        val handler = Handler(Looper.getMainLooper())
        binding.loading.isVisible = state.requestDeleteContent is Loading

        if (state.requestContents.complete) {
            binding.refreshLayout.isRefreshing = false
        }
        handler.post {
            if (state.listContents.size > 4) {
                binding.tvNoOfAttachments.visibility = View.VISIBLE
                binding.tvNoOfAttachments.text = getString(R.string.text_multiple_attachment, state.listContents.size)
            } else {
                binding.tvNoOfAttachments.visibility = View.GONE
            }
        }

        if (state.requestContents is Success) {
            binding.clParent.addView(makeFab(requireContext(), state))
        }

        if (state.listContents.isEmpty()) requireActivity().onBackPressed()

        epoxyController.requestModelBuild()
    }

    private fun makeFab(context: Context, state: TaskDetailViewState) =
        FloatingActionButton(context).apply {
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                // TODO: define margins
                setMargins(
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics)
                        .toInt()
                )
            }
            id = R.id.fab_add_attachments
            contentDescription = context.getString(R.string.text_add_attachments)
            setImageResource(R.drawable.ic_add_fab)
            setOnClickListener {
                showCreateSheet(state)
            }
        }

    private fun epoxyController() = simpleController(viewModel) { state ->

        if (state.listContents.isNotEmpty()) {
            state.listContents.forEach { obj ->
                listViewAttachmentRow {
                    id(stableId(obj))
                    data(obj)
                    clickListener { model, _, _, _ -> onItemClicked(model.data()) }
                    deleteContentClickListener { model, _, _, _ -> deleteContentPrompt(model.data()) }
                }
            }
        }
    }

    private fun onItemClicked(contentEntry: Entry) {
        if (!contentEntry.isUpload)
            viewModel.execute(ActionOpenWith(Entry.convertContentEntryToEntry(contentEntry, MimeType.isDocFile(contentEntry.mimeType))))
    }

    override fun onEntryCreated(entry: ParentEntry) {
        if (isAdded)
            startActivity(
                Intent(requireActivity(), LocalPreviewActivity::class.java)
                    .putExtra(LocalPreviewActivity.KEY_ENTRY_OBJ, entry as Entry)
            )
    }
}
