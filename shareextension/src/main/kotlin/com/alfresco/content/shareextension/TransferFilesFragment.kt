package com.alfresco.content.shareextension

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.content.data.Entry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.listview.databinding.FragmentTransferFilesListBinding
import com.alfresco.content.listview.listViewMessage
import com.alfresco.content.listview.listViewRow
import com.alfresco.content.simpleController
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
}
