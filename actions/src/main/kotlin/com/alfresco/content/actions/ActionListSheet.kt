package com.alfresco.content.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.BaseMvRxBottomSheet
import com.alfresco.content.actions.databinding.SheetActionListBinding
import com.alfresco.content.data.Entry
import com.alfresco.content.mimetype.MimeType

class ActionListSheet() : BaseMvRxBottomSheet() {
    private val viewModel: ActionListViewModel by fragmentViewModel()
    private lateinit var binding: SheetActionListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SheetActionListBinding.inflate(inflater, container, false)
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
                        dismiss()
                    }
                }
            }
        }
    }

    companion object {
        fun with(entry: Entry) = ActionListSheet().apply {
            arguments = bundleOf(MvRx.KEY_ARG to entry)
        }
    }
}
