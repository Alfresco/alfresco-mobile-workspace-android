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
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.common.EntryListener
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.UploadServerType
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.hideSoftInput
import com.alfresco.content.process.R
import com.alfresco.content.process.databinding.FragmentProcessBinding
import com.alfresco.content.process.ui.components.updateProcessList
import com.alfresco.content.process.ui.composeviews.FormScreen
import com.alfresco.content.process.ui.theme.AlfrescoBaseTheme
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
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
                if (it.parent.processInstanceId != null) {
                    supportActionBar?.title = it.parent.taskEntry.name.ifEmpty { getString(R.string.title_no_name) }
                } else {
                    supportActionBar?.title = getString(R.string.title_workflow)
                }
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

        when {
            state.requestStartWorkflow is Success || state.requestSaveForm is Success ||
                state.requestOutcomes is Success || state.requestClaimRelease is Success -> {
                viewModel.updateProcessList()
                requireActivity().finish()
            }

            state.requestForm is Success -> {
                val hasUploadField = state.formFields.any { it.type == FieldType.UPLOAD.value() }

                if (hasUploadField && state.parent.defaultEntries.isNotEmpty()) {
                    viewModel.fetchUserProfile()
                    viewModel.fetchAccountInfo()
                }

                if (hasUploadField) {
                    viewModel.observeUploads(state)
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

    override fun onAttachFolder(entry: ParentEntry) = withState(viewModel) {
        if (isAdded) {
            viewModel.updateFieldValue(
                viewModel.selectedField?.id ?: "",
                entry as? Entry,
                Pair(false, ""),
            )
            viewModel.selectedField = null
        }
    }

    override fun onAttachFiles(field: FieldsData, deletedFiles: MutableMap<String, Entry>) = withState(viewModel) { state ->
        if (isAdded && field.type == FieldType.UPLOAD.value()) {
            val serverUploads = field.getContentList().filter { it.uploadServer == UploadServerType.DATA_FROM_SERVER }.filterNot { item -> deletedFiles.any { it.value.id == item.id } }

            val listContents = serverUploads + viewModel.getContents(state, field.id)
            val isError = field.required && listContents.isEmpty()

            viewModel.updateFieldValue(field.id, listContents, Pair(isError, ""))

            viewModel.selectedField = null
        }
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
