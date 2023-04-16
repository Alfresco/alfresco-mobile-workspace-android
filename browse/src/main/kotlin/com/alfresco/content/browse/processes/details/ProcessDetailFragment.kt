package com.alfresco.content.browse.processes.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentTaskDetailBinding
import com.alfresco.content.browse.processes.ProcessDetailActivity
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.PageView
import com.alfresco.ui.getDrawableForAttribute

class ProcessDetailFragment : Fragment(), MavericksView {

    lateinit var binding: FragmentTaskDetailBinding
    val viewModel: ProcessDetailViewModel by activityViewModel()
    private var viewLayout: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (viewLayout == null) {
            binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
            viewLayout = binding.root
        }
        return viewLayout as View
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsManager().screenViewEvent(PageView.WorkflowView)
        (requireActivity() as ProcessDetailActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.apply {
            navigationContentDescription = getString(R.string.label_navigation_back)
            navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
            setNavigationOnClickListener {
                withState(viewModel) { state ->
                    requireActivity().onBackPressed()
                }
            }
            title = resources.getString(R.string.title_start_workflow)
        }

        showStartFormView()
        setInitData()
    }

    override fun invalidate() {
        binding.loading.isVisible = false
    }
}
