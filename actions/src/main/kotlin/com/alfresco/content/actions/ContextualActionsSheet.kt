package com.alfresco.content.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.databinding.SheetActionListBinding
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.ContextualActionData
import com.alfresco.content.data.Entry
import com.alfresco.ui.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class ContextualActionsSheet : BottomSheetDialogFragment(), MavericksView {
    val viewModel: ContextualActionsViewModel by fragmentViewModel()
    lateinit var binding: SheetActionListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = SheetActionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.decorView?.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        view?.viewTreeObserver?.addOnGlobalLayoutListener {
            val bottomSheet =
                (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                BottomSheetBehavior.from<View>(it).apply {
                    val peekAmount = 1.0
                    peekHeight = ((it.parent as View).height * peekAmount).toInt()
                }
            }
        }
    }

    override fun invalidate() = withState(viewModel) { state ->

        setHeader(state)

        binding.recyclerView.withModels {
            if (state.actions.isEmpty()) {
                actionListLoading { id("loading") }
            }
            state.actions.forEach {
                val entry = it.entry as Entry
                actionListRow {
                    id(it.title)
                    action(it)
                    clickListener { _ ->
                        AnalyticsManager().fileActionEvent(
                            entry.mimeType ?: "",
                            entry.name.substringAfterLast(".", ""),
                            it.eventName,
                        )
                        viewModel.execute(it)
                        dismiss()
                    }
                }
            }
        }
    }

    companion object {
        fun with(contextualActionData: ContextualActionData) = ContextualActionsSheet().apply {
            arguments = bundleOf(Mavericks.KEY_ARG to contextualActionData)
        }
    }
}
