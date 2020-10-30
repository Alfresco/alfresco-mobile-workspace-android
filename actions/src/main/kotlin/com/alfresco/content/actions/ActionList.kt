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
import com.alfresco.content.actions.databinding.FragmentListItemActionsBinding
import com.alfresco.content.data.Entry
import com.alfresco.content.mimetype.MimeType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.lang.ref.WeakReference

class ActionListFragment(parent: ActionListSheet) : BaseMvRxFragment(R.layout.fragment_list_item_actions) {
    private val viewModel: ActionListViewModel by fragmentViewModel()
    private val parent = WeakReference(parent)
    private lateinit var binding: FragmentListItemActionsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListItemActionsBinding.inflate(inflater, container, false)
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
            state.actions.forEach {
                actionListRow {
                    id(it.title)
                    action(it)
                    clickListener { _ ->
                        viewModel.execute(it)
                        parent.get()?.dismiss()
                    }
                }
            }
        }
    }
}

class ActionListSheet(private val entry: Entry) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.sheet_list_item_actions, container, false)

        if (savedInstanceState == null) {
            val contentFragment = ActionListFragment(this)
            contentFragment.arguments = bundleOf(MvRx.KEY_ARG to entry)
            childFragmentManager
                .beginTransaction()
                .replace(R.id.content, contentFragment)
                .commit()
        }

        return view
    }
}
