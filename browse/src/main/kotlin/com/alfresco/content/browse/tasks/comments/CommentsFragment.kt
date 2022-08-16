package com.alfresco.content.browse.tasks.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.parentFragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentCommentsBinding
import com.alfresco.content.browse.tasks.detail.TaskDetailViewModel
import com.alfresco.content.browse.tasks.detail.listViewCommentRow
import com.alfresco.content.simpleController

/**
 * Marked as CommentsFragment class
 */
class CommentsFragment : Fragment(), MavericksView {

    val viewModel: TaskDetailViewModel by parentFragmentViewModel()
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

        binding.recyclerView.setController(epoxyController)

        epoxyController.adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                println("CommentsFragment.onItemRangeInserted")
                if (positionStart == 0) {
                    // @see: https://github.com/airbnb/epoxy/issues/224
                    binding.recyclerView.layoutManager?.scrollToPosition(0)
                }
            }
        })
        setListeners()
    }

    private fun setListeners() {
        binding.refreshLayout.setOnRefreshListener {
            viewModel.getComments()
        }

        binding.iconSend.setOnClickListener {
            val message = binding.commentInput.text.toString().trim()
            viewModel.addComment(message)
            binding.commentInput.setText("")
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        if (state.requestComments.complete) {
            binding.refreshLayout.isRefreshing = false
        }

        epoxyController.requestModelBuild()

        if (state.listComments.size > 1) {
            binding.tvNoOfComments.visibility = View.VISIBLE
            binding.tvNoOfComments.text = getString(R.string.text_multiple_comments, state.listComments.size)
        } else {
            binding.tvNoOfComments.visibility = View.GONE
        }
    }

    private fun epoxyController() = simpleController(viewModel) { state ->

        if (state.listComments.isNotEmpty()) {
            state.listComments.forEach { obj ->
                listViewCommentRow {
                    id(obj.id)
                    data(obj)
                }
            }
            binding.recyclerView.post {
                binding.recyclerView.scrollToPosition(state.listComments.size - 1)
            }
        }
    }
}
