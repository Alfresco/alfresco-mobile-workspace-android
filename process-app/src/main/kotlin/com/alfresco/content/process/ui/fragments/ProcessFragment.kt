package com.alfresco.content.process.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.common.EntryListener
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.PageView
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.hideSoftInput
import com.alfresco.content.process.R
import com.alfresco.content.process.databinding.FragmentProcessBinding
import com.alfresco.content.process.ui.components.updateProcessList
import com.alfresco.content.process.ui.composeviews.FormScreen
import com.alfresco.content.process.ui.theme.AlfrescoBaseTheme
import com.alfresco.kotlin.FilenameComparator
import com.alfresco.list.merge
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference

class ProcessFragment : Fragment(), MavericksView, EntryListener {

    val viewModel: FormViewModel by activityViewModel()
    lateinit var binding: FragmentProcessBinding
    private var viewLayout: View? = null
    private var menu: Menu? = null
    private var isExecuted = false
    private var confirmContentQueueDialog = WeakReference<AlertDialog>(null)
    private var oldSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private fun showSnackBar(message: String) {
        val snackbar = Snackbar.make(binding.flComposeParent, message, Snackbar.LENGTH_SHORT)
        if (oldSnackbar == null || oldSnackbar?.isShownOrQueued == false) {
            oldSnackbar?.dismiss()
            snackbar.show()
            oldSnackbar = snackbar
        }
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setListener(this)

        val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        withState(viewModel) {
            if (it.parent.processInstanceId != null) {
                AnalyticsManager().screenViewEvent(PageView.TaskFormView)
                supportActionBar?.title = it.parent.taskEntry.name.ifEmpty { getString(R.string.title_no_name) }
            } else {
                AnalyticsManager().screenViewEvent(PageView.StartFormView)
            }
        }

        supportActionBar?.setHomeActionContentDescription(getString(R.string.label_navigation_back))

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AlfrescoBaseTheme {
                    FormScreen(
                        navController = findNavController(),
                        viewModel = viewModel,
                        this@ProcessFragment,
                    )
                }
            }
        }

        binding.flComposeParent.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideSoftInput()
            }
            false
        }

        viewModel.onLinkContentToProcess = {
            withState(viewModel) { state ->
                it.second?.apply {
                    val listContents = listOf(it.first)
                    val isError = this.required && listContents.isEmpty()
                    viewModel.updateFieldValue(this.id, listContents, Pair(isError, ""))
                }
            }
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.loading.isVisible = state.requestForm is Loading || state.requestStartWorkflow is Loading ||
            state.requestSaveForm is Loading || state.requestOutcomes is Loading || state.requestProfile is Loading ||
            state.requestAccountInfo is Loading || state.requestContent is Loading

        handleError(state)
        when {
            state.requestStartWorkflow is Success || state.requestSaveForm is Success ||
                state.requestOutcomes is Success || state.requestClaimRelease is Success -> {
                viewModel.updateProcessList()
                requireActivity().finish()
            }

            state.requestForm is Success -> {
                val hasUploadField = state.formFields.any { it.type == FieldType.UPLOAD.value() }

                if (state.parent.defaultEntries.isNotEmpty()) {
                    if (hasUploadField) {
                        viewModel.fetchUserProfile()
                        viewModel.fetchAccountInfo()
                    } else {
                        showSnackBar(getString(R.string.error_no_upload_fields))
                    }
                }

                if (hasUploadField) {
                    viewModel.observeUploads(state)
                    val fields = state.formFields
                    fields.forEach { field ->
                        if (field.type == FieldType.UPLOAD.value()) {
                            field.getContentList(state.parent.processDefinitionId).forEach(OfflineRepository()::addServerEntry)
                        }
                    }
                }

                viewModel.resetRequestState(state.requestForm)
            }

            state.requestAccountInfo is Success -> {
                val uploadingFields = state.formFields.filter { it.type == FieldType.UPLOAD.value() }
                val field = uploadingFields.find { it.params?.multiple == false } ?: uploadingFields.firstOrNull()
                val sourceName = state.listAccountInfo.firstOrNull()?.sourceName ?: ""

                if (!isExecuted) {
                    isExecuted = true
                    state.parent.defaultEntries.map { entry ->
                        viewModel.linkContentToProcess(state, entry, sourceName, field)
                    }
                }
                viewModel.resetRequestState(state.requestAccountInfo)
            }

            state.requestContent is Success -> {
                viewModel.resetRequestState(state.requestContent)
            }
        }
        menu?.findItem(R.id.action_info)?.isVisible = state.parent.processInstanceId != null
    }

    private fun handleError(state: FormViewState) {
        when {
            state.requestStartWorkflow is Fail<*> || state.requestForm is Fail<*> ||
                state.requestSaveForm is Fail<*> || state.requestProfile is Fail<*> || state.request is Fail<*> ||
                state.requestOutcomes is Fail<*> || state.requestContent is Fail<*> || state.requestProcessDefinition is Fail<*> ||
                state.requestClaimRelease is Fail<*> || state.requestFormVariables is Fail<*> || state.requestAccountInfo is Fail<*> -> {
                showSnackBar(getString(R.string.error_process_failure))
            }
        }
    }

    override fun onAttachFolder(entry: ParentEntry) = withState(viewModel) {
        if (isAdded && viewModel.selectedField?.type == FieldType.SELECT_FOLDER.value()) {
            viewModel.updateFieldValue(
                viewModel.selectedField?.id ?: "",
                entry as? Entry,
                Pair(false, ""),
            )
            viewModel.selectedField = null
        }
    }

    override fun onAttachFiles(field: FieldsData) = withState(viewModel) { state ->
        if (isAdded && field.type == FieldType.UPLOAD.value()) {
            val listContents = mergeInUploads(field.getContentList(state.parent.processDefinitionId), viewModel.getContents(state, field.id))
            val isError = field.required && listContents.isEmpty()

            viewModel.updateFieldValue(field.id, listContents, Pair(isError, ""))

            viewModel.selectedField = null
        }
    }

    private fun mergeInUploads(base: List<Entry>, uploads: List<Entry>): List<Entry> {
        if (uploads.isEmpty()) {
            return emptyList()
        }

        return merge(base, uploads, includeRemainingRight = true) { left: Entry, right: Entry ->
            FilenameComparator.compare(left.name, right.name)
        }.distinctBy { it.id.ifEmpty { it.boxId } }
    }

    /**
     * It will prompt if user trying to start workflow and if any of content file is in uploaded
     */
    fun confirmContentQueuePrompt() {
        val oldDialog = confirmContentQueueDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setTitle(getString(R.string.title_content_in_queue))
            .setMessage(getString(R.string.message_content_in_queue))
            .setNegativeButton(getString(R.string.dialog_negative_button_task), null)
            .setPositiveButton(getString(R.string.dialog_positive_button_task)) { _, _ ->
                viewModel.optionsModel?.let {
                    viewModel.performOutcomes(
                        it,
                    )
                }
            }
            .show()
        confirmContentQueueDialog = WeakReference(dialog)
    }
}
