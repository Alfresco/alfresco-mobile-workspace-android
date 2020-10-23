package com.alfresco.content.viewer

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.ActionBarFragment
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
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ViewerArgs(
    val id: String,
    val title: String,
    val type: String
) : Parcelable {
    companion object {
        private const val ID_KEY = "id"
        private const val TITLE_KEY = "title"
        private const val TYPE_KEY = "type"

        fun with(args: Bundle): ViewerArgs {
            return ViewerArgs(
                args.getString(ID_KEY, ""),
                args.getString(TITLE_KEY, ""),
                args.getString(TYPE_KEY, "")
            )
        }
    }
}

class ViewerFragment : BaseMvRxFragment(), LoadingListener {

    private lateinit var args: ViewerArgs
    private val viewModel: ViewerViewModel by fragmentViewModelWithArgs { args }
    private lateinit var binding: ViewerBinding
    private var childFragment: ChildViewerFragment? = null

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
            this.childFragment?.setLoadingListener(this)
        }
    }

    override fun onContentLoaded() {
        binding.loading.visibility = View.GONE
        if (this.childFragment?.showInfoWhenLoaded() == true) {
            binding.info.visibility = View.VISIBLE
        } else {
            binding.info.visibility = View.GONE
        }
        binding.status.text = ""
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.title.text = args.title
        val type = MimeType.with(state.entry?.mimeType)
        binding.icon.setImageDrawable(resources.getDrawable(type.icon, requireContext().theme))

        if (state.entry != null) {
            val fragment = ActionBarFragment().apply {
                arguments = bundleOf(MvRx.KEY_ARG to state.entry)
            }
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.action_list_bar, fragment)
                .commit()
        }

        if (state.ready) {
            if (state.viewerType != null) {
                if (childFragmentManager.findFragmentByTag(state.viewerType.toString()) == null) {
                    val fragment = viewerFragment(state.viewerType, typeArgs(state.viewerUri ?: ""))
                    childFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, fragment, state.viewerType.toString())
                        .commit()
                }
                binding.status.text = getString(R.string.info_fetching_content)
            } else {
                binding.loading.visibility = View.GONE
                binding.status.text = getString(R.string.error_preview_not_available)
            }
        } else {
            binding.status.text = getString(R.string.info_creating_rendition)
        }
    }

    private fun typeArgs(uri: String): ChildViewerArgs {
        return ChildViewerArgs(args.id, uri, args.type)
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
}
