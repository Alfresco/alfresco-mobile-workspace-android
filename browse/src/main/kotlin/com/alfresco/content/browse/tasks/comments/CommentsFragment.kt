package com.alfresco.content.browse.tasks.comments

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentCommentsBinding
import com.alfresco.content.browse.tasks.detail.TaskDetailViewModel
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.PageView
import com.alfresco.content.hideSoftInput
import com.alfresco.content.simpleController
import com.alfresco.ui.getDrawableForAttribute
import com.google.android.material.textfield.TextInputEditText

/**
 * Marked as CommentsFragment class
 */
class CommentsFragment : Fragment(), MavericksView {

    val viewModel: TaskDetailViewModel by activityViewModel()
    private lateinit var binding: FragmentCommentsBinding
    private val epoxyController: AsyncEpoxyController by lazy { epoxyController() }
    private var isScrolled = false

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

        AnalyticsManager().screenViewEvent(PageView.Comments)

        binding.toolbar.apply {
            navigationContentDescription = getString(R.string.label_navigation_back)
            navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
            title = resources.getString(R.string.title_comments)
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
        updateSendIconView(binding.commentInput.text.toString())

        withState(viewModel) { state ->
            when {
                state.parent?.endDate != null -> binding.clAddComment.visibility = View.GONE
                else -> binding.clAddComment.visibility = View.VISIBLE
            }
        }

        setListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        binding.refreshLayout.setOnRefreshListener {
            viewModel.getComments()
        }

        binding.iconSend.setOnClickListener {
            hideSoftInput()
            val message = binding.commentInput.text.toString().trim()
            viewModel.addComment(message)
            binding.commentInput.setText("")
        }

        binding.commentInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // no-op
            }

            override fun afterTextChanged(s: Editable?) {
                updateSendIconView(s.toString())
            }
        })

        requireActivity().window.decorView.viewTreeObserver.addOnGlobalLayoutListener {
            if (isAdded) {
                val r = Rect()
                requireActivity().window.decorView.getWindowVisibleDisplayFrame(r)
                val height = requireActivity().window.decorView.height
                if (height - r.bottom > height * 0.1399 && !isScrolled) {
                    // keyboard is open
                    withState(viewModel) { state ->
                        binding.recyclerView.scrollToPosition(state.listComments.size - 1)
                    }
                    isScrolled = true
                } else isScrolled = false
            }
        }

        binding.recyclerView.setOnTouchListener { _, _ ->
            hideSoftInput()
            return@setOnTouchListener false
        }

    }

    private fun updateSendIconView(text: String) {
        if (text.isEmpty()) {
            binding.iconSend.isSaveEnabled = false
            binding.iconSend.setImageResource(R.drawable.icon_send_disabled)
        } else {
            binding.iconSend.isEnabled = true
            binding.iconSend.setImageResource(R.drawable.icon_send)
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        if (state.requestComments.complete) {
            binding.refreshLayout.isRefreshing = false
        }

        if (viewModel.isAddComment) {
            showKeyboard(binding.commentInput)
            viewModel.isAddComment = false
        }

        epoxyController.requestModelBuild()

        if (state.listComments.size > 1) {
            binding.tvNoOfComments.visibility = View.VISIBLE
            binding.tvNoOfComments.text = getString(R.string.text_multiple_comments, state.listComments.size)
        } else {
            binding.tvNoOfComments.visibility = View.GONE
        }
    }

    private fun showKeyboard(edt: TextInputEditText) {
        edt.requestFocus()
        val imm: InputMethodManager =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(edt, 0)
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
