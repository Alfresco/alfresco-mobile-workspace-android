package com.alfresco.content.browse.processes.status

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentTaskStatusBinding
import com.alfresco.content.browse.processes.ProcessDetailActivity
import com.alfresco.content.browse.tasks.detail.TaskDetailViewModel
import com.alfresco.content.browse.tasks.detail.isAssigneeAndLoggedInSame
import com.alfresco.content.browse.tasks.detail.isTaskCompleted
import com.alfresco.content.common.BaseActivity
import com.alfresco.content.component.ComponentBuilder
import com.alfresco.content.component.ComponentData
import com.alfresco.content.component.ComponentMetaData
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.PageView
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.setSafeOnClickListener
import com.alfresco.ui.getDrawableForAttribute
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * TaskStatusFragment
 */
class TaskStatusFragment : Fragment(), MavericksView {

    val viewModel: TaskDetailViewModel by activityViewModel()
    lateinit var binding: FragmentTaskStatusBinding
    private var viewLayout: View? = null
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (viewLayout == null) {
            binding = FragmentTaskStatusBinding.inflate(inflater, container, false)
            viewLayout = binding.root
        }
        return viewLayout as View
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsManager().screenViewEvent(PageView.WorkflowTaskStatusView)

        var instanceActivity: AppCompatActivity? = null
        when (requireActivity()) {
            is ProcessDetailActivity -> instanceActivity = (requireActivity() as ProcessDetailActivity)
            is BaseActivity -> instanceActivity = (requireActivity() as BaseActivity)
        }
        instanceActivity?.apply {
            setSupportActionBar(binding.toolbar)
            withState(viewModel) { state ->
                if (!viewModel.isTaskCompleted(state) && viewModel.isAssigneeAndLoggedInSame(state.parent?.assignee)) {
                    setHasOptionsMenu(true)
                }
            }

            binding.toolbar.apply {
                navigationContentDescription = getString(R.string.label_navigation_back)
                navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
                setNavigationOnClickListener {
                    withState(viewModel) { state ->
                        viewModel.updateTaskStatusAndName(viewModel.previousTaskFormStatus, state.parent?.comment)
                    }
                    requireActivity().onBackPressed()
                }
                title = resources.getString(R.string.title_status)
            }
        }

        withState(viewModel) { state ->
            binding.commentInput.setText(state.parent?.comment ?: "")
            viewModel.previousTaskFormStatus = state.parent?.taskFormStatus ?: ""
            if (!viewModel.isTaskCompleted(state) && viewModel.isAssigneeAndLoggedInSame(state.parent?.assignee)) {
                setListeners()
            } else {
                binding.commentInput.isEnabled = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_task_status, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                viewModel.saveForm(binding.commentInput.text.toString().trim())
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setListeners() {
        binding.clStatus.setSafeOnClickListener {
            withState(viewModel) { state ->
                state.parent?.let {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val result = showComponentSheetDialog(requireContext(), state.parent)
                        result?.let { dataObj ->
                            viewModel.updateTaskStatus(dataObj)
                        }
                    }
                }
            }
        }
    }

    private suspend fun showComponentSheetDialog(
        context: Context,
        taskEntry: TaskEntry,
    ) = withContext(dispatcher) {
        suspendCoroutine {
            val componentData = ComponentData.with(taskEntry)
            ComponentBuilder(context, componentData)
                .onApply { name, query, _ ->
                    executeContinuation(it, name, query)
                }
                .onReset { name, query, _ ->
                    executeContinuation(it, name, query)
                }
                .onCancel {
                    it.resume(null)
                }
                .show()
        }
    }

    private fun executeContinuation(continuation: Continuation<ComponentMetaData?>, name: String, query: String) {
        continuation.resume(ComponentMetaData(name = name, query = query))
    }

    override fun invalidate() = withState(viewModel) { state ->

        binding.loading.isVisible = (state.requestSaveForm is Loading)

        if (state.requestSaveForm.invoke()?.code() == 200) {
            viewModel.updateTaskStatusAndName(state.parent?.taskFormStatus, binding.commentInput.text.toString().trim())
            requireActivity().onBackPressed()
        }

        binding.tvStatus.text = state.parent?.taskFormStatus
    }
}
