package com.alfresco.content.browse.tasks.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentCommentsBinding
import com.alfresco.content.simpleController
import com.alfresco.ui.getDrawableForAttribute

/**
 * Marked as CommentsFragment class
 */
class CommentsFragment : Fragment(), MavericksView {

    val viewModel: TaskDetailViewModel by fragmentViewModel()
    private lateinit var binding: FragmentCommentsBinding
    private val epoxyController: AsyncEpoxyController by lazy { epoxyController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.apply {
            navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
            title = resources.getString(R.string.title_comments)
        }

        binding.recyclerView.setController(epoxyController)
    }

    override fun invalidate() {
        epoxyController.requestModelBuild()
    }

    private fun epoxyController() = simpleController(viewModel) { state ->

        if (state.listComments.isNotEmpty()) {
            state.listComments.forEach { obj ->
                listViewCommentRow {
                    id(obj.id)
                    data(obj)
                }
            }
        }
    }
}
