package com.alfresco.content.browse.processes.details

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.DATE_FORMAT_1
import com.alfresco.content.DATE_FORMAT_4
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentTaskDetailBinding
import com.alfresco.content.browse.processes.ProcessDetailActivity
import com.alfresco.content.common.updatePriorityView
import com.alfresco.content.component.ComponentBuilder
import com.alfresco.content.component.ComponentData
import com.alfresco.content.component.ComponentMetaData
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.PageView
import com.alfresco.content.getFormattedDate
import com.alfresco.ui.getDrawableForAttribute
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Marked as ProcessDetailFragment
 */
class ProcessDetailFragment : Fragment(), MavericksView {

    lateinit var binding: FragmentTaskDetailBinding
    val viewModel: ProcessDetailViewModel by activityViewModel()
    private var viewLayout: View? = null
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main

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
        setListeners()
        setListeners()
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.loading.isVisible = false
        setData(state)
    }

    private fun setData(state: ProcessDetailViewState) {
        val dataEntry = state.entry
        binding.tvTitle.text = dataEntry.name
        binding.tvDescription.text = dataEntry.description.ifEmpty { requireContext().getString(R.string.empty_description) }
        binding.tvAttachedTitle.text = getString(R.string.text_attached_files)
        binding.tvDueDateValue.text =
            if (dataEntry.formattedDueDate.isNullOrEmpty()) requireContext().getString(R.string.empty_no_due_date) else dataEntry.formattedDueDate?.getFormattedDate(DATE_FORMAT_1, DATE_FORMAT_4)
        binding.tvNoAttachedFilesError.text = getString(R.string.no_attached_files)
        binding.completeButton.text = getString(R.string.title_start_workflow)
        binding.tvPriorityValue.updatePriorityView(state.entry.priority)
    }

    internal suspend fun showComponentSheetDialog(
        context: Context,
        componentData: ComponentData
    ) = withContext(dispatcher) {
        suspendCoroutine {

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

    internal suspend fun showSearchUserComponentDialog(
        context: Context,
        taskEntry: TaskEntry
    ) = withContext(dispatcher) {
        suspendCoroutine {

            SearchUserComponentBuilder(context, taskEntry)
                .onApply { userDetails ->
                    it.resume(userDetails)
                }
                .onCancel {
                    it.resume(null)
                }
                .show()
        }
    }
}
