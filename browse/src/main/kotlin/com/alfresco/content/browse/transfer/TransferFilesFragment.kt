package com.alfresco.content.browse.transfer

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.ActionSyncNow
import com.alfresco.content.browse.R
import com.alfresco.content.data.Entry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.listview.databinding.FragmentTransferFilesListBinding
import com.alfresco.content.listview.listViewMessage
import com.alfresco.content.listview.listViewRow
import com.alfresco.content.simpleController
import com.alfresco.events.emit
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.parcelize.Parcelize

/**
 * Mark as TransferFilesArgs
 */
@Parcelize
data class TransferFilesArgs(
    val extension: Boolean
) : Parcelable {
    companion object {
        private const val EXTENSION_KEY = "extension"

        /**
         * return the TransferFilesArgs obj
         */
        fun with(args: Bundle): TransferFilesArgs {
            return TransferFilesArgs(
                args.getBoolean(EXTENSION_KEY, false)
            )
        }
    }
}

/**
 * Mark as TransferFilesFragment
 */
class TransferFilesFragment : Fragment(), MavericksView {

    private lateinit var args: TransferFilesArgs
    private val viewModel: TransferFilesViewModel by fragmentViewModelWithArgs { args }
    private lateinit var binding: FragmentTransferFilesListBinding
    private val epoxyController: AsyncEpoxyController by lazy { epoxyController() }
    private var fab: ExtendedFloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = TransferFilesArgs.with(requireArguments())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTransferFilesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.setController(epoxyController)
    }

    override fun invalidate() = withState(viewModel) { state ->

        if (fab == null) {
            fab = makeFab(requireContext()).apply {
                visibility = View.INVISIBLE // required for animation
            }
            (view as ViewGroup).addView(fab)
        }

        fab?.apply {
            if (state.entries.count() > 0) {
                show()
            } else {
                hide()
            }

            isEnabled = state.syncNowEnabled
        }

        epoxyController.requestModelBuild()
    }

    private fun epoxyController() = simpleController(viewModel) { state ->
        if (state.entries.isEmpty()) {
            val args = viewModel.emptyMessageArgs()
            listViewMessage {
                id("empty_message")
                iconRes(args.first)
                title(args.second)
                message(args.third)
            }
        } else if (state.entries.isNotEmpty()) {
            state.entries.forEach {
                listViewRow {
                    id(stableId(it))
                    data(it)
                }
            }
        }
    }

    private fun stableId(entry: Entry): String =
        if (entry.isUpload) entry.boxId.toString()
        else entry.id

    private fun makeFab(context: Context) =
        ExtendedFloatingActionButton(context).apply {
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                setMargins(0, 0, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt())
            }
            text = context.getText(R.string.offline_sync_button_title)
            gravity = Gravity.CENTER
            setOnClickListener {
                onSyncButtonClick()
            }
        }

    private fun onSyncButtonClick() {
        if (viewModel.canSyncOverCurrentNetwork()) {
            startSync(false)
        } else {
//            makeSyncUnavailablePrompt().show()
        }
    }

    private fun startSync(overrideNetwork: Boolean) =
        lifecycleScope.emit(ActionSyncNow(overrideNetwork))
}
