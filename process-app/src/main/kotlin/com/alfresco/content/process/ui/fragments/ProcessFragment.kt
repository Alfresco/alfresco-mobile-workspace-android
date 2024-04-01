package com.alfresco.content.process.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.common.EntryListener
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.process.R
import com.alfresco.content.process.databinding.FragmentProcessBinding
import com.alfresco.content.process.ui.components.updateProcessList
import com.alfresco.content.process.ui.composeviews.FormScreen
import com.alfresco.content.process.ui.theme.AlfrescoBaseTheme

class ProcessFragment : Fragment(), MavericksView, EntryListener {

    val viewModel: FormViewModel by activityViewModel()
    lateinit var binding: FragmentProcessBinding
    private var viewLayout: View? = null
    private var menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentProcessBinding.inflate(inflater, container, false)
        viewLayout = binding.root
        return viewLayout as View
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_process, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        this.menu = menu
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_info -> {
                withState(viewModel) { state ->
                    val entry = state.parent.taskEntry
                    val intent = Intent(
                        requireActivity(),
                        Class.forName("com.alfresco.content.app.activity.TaskViewerActivity"),
                    ).apply {
                        putExtra(Mavericks.KEY_ARG, entry)
                    }
                    startActivity(intent)
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setListener(this)

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
        binding.loading.isVisible = state.requestForm is Loading || state.requestStartWorkflow is Loading ||
            state.requestSaveForm is Loading || state.requestOutcomes is Loading

        if (state.requestStartWorkflow is Success || state.requestSaveForm is Success) {
            viewModel.updateProcessList()
            requireActivity().finish()
        }

        menu?.findItem(R.id.action_info)?.isVisible = state.parent.processInstanceId != null
    }

    override fun onAttachFolder(entry: ParentEntry) = withState(viewModel) {
        if (isAdded) {
            viewModel.updateFieldValue(
                viewModel.folderFieldId,
                (entry as Entry).id,
                it,
                Pair(false, ""),
            )
            viewModel.folderFieldId = ""
        }
    }
}
