package com.alfresco.content.actions

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
import com.alfresco.content.actions.databinding.SheetActionListBinding
import com.alfresco.content.data.Entry
import com.alfresco.ui.BottomSheetDialogFragment

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

    override fun invalidate() = withState(viewModel) { state ->
        binding.header.apply {
            parentTitle.contentDescription = getString(R.string.title_select_workflow)
            icon.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_start_workflow, context?.theme))
            title.text = getString(R.string.title_select_workflow)
        }

        binding.recyclerView.withModels {
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
