package com.alfresco.content.browse.processes.sheet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.actionListLoading
import com.alfresco.content.actions.databinding.SheetActionListBinding
import com.alfresco.content.actions.listRowProcessDefinitions
import com.alfresco.content.browse.R
import com.alfresco.content.browse.processes.ProcessDetailActivity
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ProcessEntry
import com.alfresco.ui.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * Marked as ProcessDefinitionsSheet
 */
class ProcessDefinitionsSheet : BottomSheetDialogFragment(), MavericksView {

    private val viewModel: ProcessDefinitionsViewModel by fragmentViewModel()
    private lateinit var binding: SheetActionListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetActionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.setTitle(" ")
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
        binding.header.apply {
            parentTitle.contentDescription = getString(R.string.title_select_workflow)
            icon.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_start_workflow, context?.theme))
            title.text = getString(R.string.title_select_workflow)
        }

        binding.recyclerView.withModels {
            if (state.listProcessDefinitions == null) {
                actionListLoading { id("loading") }
            }
            state.listProcessDefinitions?.forEach {
                listRowProcessDefinitions {
                    id(it.id)
                    processDefinition(it)
                    clickListener { model, _, _, _ ->
                        val processEntry = ProcessEntry.with(model.processDefinition(), state.entry)
                        startActivity(
                            Intent(requireActivity(), ProcessDetailActivity::class.java)
                                .putExtra(Mavericks.KEY_ARG, processEntry)
                        )
                        dismiss()
                    }
                }
            }
        }
    }

    companion object {
        /**
         * returns the instance of ProcessDefinitionsSheet with attached entry as bundle
         */
        fun with(entry: Entry) = ProcessDefinitionsSheet().apply {
            arguments = bundleOf(Mavericks.KEY_ARG to entry)
        }
    }
}
