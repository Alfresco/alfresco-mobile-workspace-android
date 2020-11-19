package com.alfresco.content.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.databinding.FragmentActionListBinding
import com.alfresco.content.data.Entry
import com.alfresco.content.mimetype.MimeType
import com.alfresco.ui.BottomSheetDialogFragment

internal class ActionListFragment() : BaseMvRxFragment() {
    private val viewModel: ActionListViewModel by fragmentViewModel()
    private lateinit var binding: FragmentActionListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun invalidate() = withState(viewModel) { state ->
        val type = when (state.entry.type) {
            Entry.Type.Site -> MimeType.LIBRARY
            Entry.Type.Folder -> MimeType.FOLDER
            else -> MimeType.with(state.entry.mimeType)
        }

        binding.header.apply {
            icon.setImageDrawable(ResourcesCompat.getDrawable(resources, type.icon, context?.theme))
            title.text = state.entry.title
        }

        binding.recyclerView.withModels {
            if (state.actions.isEmpty()) {
                actionListLoading { id("loading") }
            }
            state.actions.forEach {
                actionListRow {
                    id(it.title)
                    action(it)
                    clickListener { _ ->
                        viewModel.execute(it)
                        (parentFragment as ActionListSheet).dismiss()
                    }
                }
            }
        }
    }
}

class ActionListSheet() : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.sheet_action_list, container, false)

        if (savedInstanceState == null) {
            val contentFragment = ActionListFragment()
            contentFragment.arguments = arguments
            childFragmentManager
                .beginTransaction()
                .replace(R.id.content, contentFragment)
                .commit()
        }

        return view
    }

    companion object {
        fun with(entry: Entry) = ActionListSheet().apply {
            arguments = bundleOf(MvRx.KEY_ARG to entry)
        }
    }
}
