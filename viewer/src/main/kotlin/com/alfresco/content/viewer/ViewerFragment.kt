package com.alfresco.content.viewer

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.ActionBarFragment
import com.alfresco.content.data.Entry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.mimetype.MimeType
import com.alfresco.content.viewer.common.ChildViewerArgs
import com.alfresco.content.viewer.common.ChildViewerFragment
import com.alfresco.content.viewer.common.LoadingListener
import com.alfresco.content.viewer.databinding.ViewerBinding
import com.alfresco.content.viewer.image.ImageViewerFragment
import com.alfresco.content.viewer.media.MediaViewerFragment
import com.alfresco.content.viewer.pdf.PdfViewerFragment
import com.alfresco.content.viewer.text.TextViewerFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.parcelize.Parcelize

@Parcelize
data class ViewerArgs(
    val id: String,
    val title: String,
    val mode: String
) : Parcelable {
    companion object {
        private const val ID_KEY = "id"
        private const val TITLE_KEY = "title"
        private const val MODE_KEY = "mode"

        fun with(args: Bundle): ViewerArgs {
            return ViewerArgs(
                args.getString(ID_KEY, ""),
                args.getString(TITLE_KEY, ""),
                args.getString(MODE_KEY, "")
            )
        }
    }
}

class ViewerFragment : BaseMvRxFragment() {

    private lateinit var args: ViewerArgs
    private val viewModel: ViewerViewModel by fragmentViewModelWithArgs { args }
    private lateinit var binding: ViewerBinding
    private var childFragment: ChildViewerFragment? = null

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
        savedInstanceState: Bundle?
    ): View? {
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
            this.childFragment = childFragment
                .apply {
                    loadingListener = viewerLoadingListener
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        childFragment?.loadingListener = null
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.title.text = args.title
        val type = MimeType.with(state.entry?.mimeType)
        binding.icon.setImageDrawable(resources.getDrawable(type.icon, requireContext().theme))

        if (state.entry != null) {
            configureActionBar(state.entry)
        }

        if (state.ready) {
            if (state.viewerType != null) {
                configureViewer(
                    state.viewerUri ?: "",
                    state.viewerType,
                    state.entry?.mimeType ?: ""
                )
                show(Status.LoadingPreview)
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
        val fragment = ActionBarFragment().apply {
            arguments = bundleOf(MvRx.KEY_ARG to entry)
        }
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.action_list_bar, fragment)
            .commit()
    }

    private fun configureViewer(
        viewerUri: String,
        viewerType: ViewerType,
        mimeType: String
    ) {
        val tag = viewerType.toString()
        if (childFragmentManager.findFragmentByTag(tag) == null) {
            val args = typeArgs(
                viewerUri,
                mimeType
            )
            val fragment = viewerFragment(viewerType, args)
            childFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment, tag)
                .commit()
        }
    }

    private fun show(s: Status) {
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
                    info.isVisible = childFragment?.showInfoWhenLoaded() == true
                    loading.isVisible = false
                    status.text = ""
                }

                Status.NotSupported -> {
                    info.isVisible = true
                    loading.isVisible = false
                    status.text = getString(R.string.error_preview_not_available)
                }
            }
        }
    }

    private fun typeArgs(uri: String, mimeType: String): ChildViewerArgs {
        return ChildViewerArgs(args.id, uri, mimeType)
    }

    private fun viewerFragment(type: ViewerType, args: ChildViewerArgs): Fragment {
        return when (type) {
            ViewerType.Pdf -> PdfViewerFragment()
            ViewerType.Image -> ImageViewerFragment()
            ViewerType.Text -> TextViewerFragment()
            ViewerType.Media -> MediaViewerFragment()
        }.apply { arguments = bundleOf(MvRx.KEY_ARG to args) }
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    private enum class Status {
        LoadingMetadata,
        PreparingPreview,
        LoadingPreview,
        PreviewLoaded,
        NotSupported
    }
}
