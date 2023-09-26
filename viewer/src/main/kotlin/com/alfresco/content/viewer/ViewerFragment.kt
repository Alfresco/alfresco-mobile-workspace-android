package com.alfresco.content.viewer

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.ContextualActionsBarFragment
import com.alfresco.content.data.ContextualActionData
import com.alfresco.content.data.Entry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.mimetype.MimeType
import com.alfresco.content.viewer.common.ChildViewerArgs
import com.alfresco.content.viewer.common.ChildViewerFragment
import com.alfresco.content.viewer.common.LoadingListener
import com.alfresco.content.viewer.databinding.ViewerBinding
import kotlinx.parcelize.Parcelize

@Parcelize
data class ViewerArgs(
    val id: String,
    val title: String,
    val mode: String,
) : Parcelable {
    companion object {
        const val ID_KEY = "id"
        const val TITLE_KEY = "title"
        const val MODE_KEY = "mode"
        const val VALUE_REMOTE = "remote"
        const val VALUE_SHARE = "share"
        const val KEY_FOLDER = "folder"

        fun with(args: Bundle): ViewerArgs {
            return ViewerArgs(
                args.getString(ID_KEY, ""),
                args.getString(TITLE_KEY, ""),
                args.getString(MODE_KEY, ""),
            )
        }
    }
}

class ViewerFragment : Fragment(), MavericksView {

    private lateinit var args: ViewerArgs

    @OptIn(InternalMavericksApi::class)
    private val viewModel: ViewerViewModel by fragmentViewModelWithArgs { args }
    private lateinit var binding: ViewerBinding
    private var childFragment: ChildViewerFragment? = null
    private var hasLoadingStatus: Boolean = false

    private val viewerLoadingListener = object : LoadingListener {
        override fun onContentLoaded() {
            show(Status.PreviewLoaded)
        }

        override fun onContentError() {
            show(Status.NotSupported)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = ViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = ViewerArgs.with(requireArguments())
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)

        if (childFragment is ChildViewerFragment) {
            this.childFragment = childFragment.apply {
                loadingListener = viewerLoadingListener
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        childFragment?.loadingListener = null
    }

    override fun invalidate() = withState(viewModel) { state ->
        if (state.entry?.name != null) {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = state.entry.name
            binding.title.text = state.entry.name
        } else binding.title.text = args.title
        val type = MimeType.with(state.entry?.mimeType)
        binding.icon.setImageDrawable(
            ResourcesCompat.getDrawable(resources, type.icon, requireContext().theme),
        )

        if (state.entry != null) {
            configureActionBar(state.entry)
        }

        if (state.ready) {
            if (state.viewerMimeType != null && state.viewerUri != null) {
                configureViewer(
                    state.viewerUri,
                    state.viewerMimeType,
                )
                if (!hasLoadingStatus) {
                    show(Status.LoadingPreview)
                }
            } else {
                show(Status.NotSupported)
            }
        } else {
            if (state.entry == null) {
                show(Status.LoadingMetadata)
            } else {
                show(Status.PreparingPreview)
            }
        }
    }

    private fun configureActionBar(entry: Entry) {
        val fragment = ContextualActionsBarFragment().apply {
            arguments = bundleOf(Mavericks.KEY_ARG to ContextualActionData.withEntries(listOf(entry)))
        }
        parentFragmentManager.beginTransaction().replace(R.id.action_list_bar, fragment).commit()
    }

    private fun configureViewer(
        viewerUri: String,
        mimeType: String,
    ) {
        val tag = mimeType
        if (childFragmentManager.findFragmentByTag(tag) == null) {
            val args = ChildViewerArgs(
                viewerUri,
                mimeType,
            )
            val fragment = ViewerRegistry.previewProvider(mimeType)?.createViewer()
            requireNotNull(fragment)
            fragment.arguments = bundleOf(Mavericks.KEY_ARG to args)

            childFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, fragment, tag).commit()
        }
    }

    private fun show(s: Status) {
        println("ViewerFragment.show ${s.name}")
        binding.apply {
            when (s) {
                Status.LoadingMetadata -> {
                    info.isVisible = false
                    loading.isVisible = true
                    status.text = ""
                }

                Status.PreparingPreview -> {
                    info.isVisible = true
                    loading.isVisible = true
                    status.text = getString(R.string.info_creating_rendition)
                }

                Status.LoadingPreview -> {
                    info.isVisible = true
                    loading.isVisible = true
                    status.text = getString(R.string.info_fetching_content)
                }

                Status.PreviewLoaded -> {
                    hasLoadingStatus = true
                    info.isVisible = childFragment?.showInfoWhenLoaded() == true
                    loading.isVisible = false
                    status.text = ""
                }

                Status.NotSupported -> {
                    hasLoadingStatus = true
                    info.isVisible = true
                    loading.isVisible = false
                    status.text = getString(R.string.error_preview_not_available)
                }
            }
        }
    }

    private enum class Status {
        LoadingMetadata, PreparingPreview, LoadingPreview, PreviewLoaded, NotSupported
    }
}
