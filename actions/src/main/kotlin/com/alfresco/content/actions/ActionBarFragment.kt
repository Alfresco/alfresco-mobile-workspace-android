package com.alfresco.content.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.alfresco.content.actions.databinding.ActionBarFragmentBinding

class ActionBarFragment : BaseMvRxFragment() {
    private val viewModel: ActionListViewModel by fragmentViewModel()
    private lateinit var binding: ActionBarFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActionBarFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.downloadButton.setOnClickListener {
            viewModel.execute(Action.Download::class.java)
        }

        binding.favoriteButton.setOnClickListener {
            viewModel.execute(Action.AddFavorite::class.java) // TODO:
        }

        binding.deleteButton.setOnClickListener {
            viewModel.execute(Action.Delete::class.java)
            requireActivity().onBackPressed()
        }
    }

    override fun invalidate() {
        // no-op
    }
}
