package com.alfresco.content.process.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.process.R
import com.alfresco.content.process.databinding.FragmentProcessBinding
import com.alfresco.content.process.ui.composeviews.FormScreen
import com.alfresco.content.process.ui.theme.AlfrescoBaseTheme

class ProcessFragment : Fragment(), MavericksView {

    val viewModel: FormViewModel by activityViewModel()
    lateinit var binding: FragmentProcessBinding
    private var viewLayout: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentProcessBinding.inflate(inflater, container, false)
        viewLayout = binding.root
        return viewLayout as View
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeActionContentDescription(getString(R.string.label_navigation_back))

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AlfrescoBaseTheme {
                    FormScreen(
                        navController = findNavController(),
                        viewModel = viewModel,
                    )
                }
            }
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.loading.isVisible = state.requestStartForm is Loading || state.requestStartWorkflow is Loading
    }
}
