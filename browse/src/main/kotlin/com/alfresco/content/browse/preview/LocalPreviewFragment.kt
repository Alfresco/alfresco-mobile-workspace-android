package com.alfresco.content.browse.preview

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentLocalPreviewBinding
import com.alfresco.content.data.Entry
import com.alfresco.content.data.UploadServerType
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.mimetype.MimeType
import com.alfresco.content.viewer.common.ChildViewerArgs
import com.alfresco.content.viewer.image.ImagePreviewProvider
import com.alfresco.content.viewer.media.MediaPreviewProvider
import com.alfresco.content.viewer.pdf.PdfPreviewProvider
import com.alfresco.content.viewer.text.TextPreviewProvider
import kotlinx.parcelize.Parcelize

/**
 * Mark as LocalPreviewArgs
 */
@Parcelize
data class LocalPreviewArgs(
    val entry: Entry? = null,
    val path: String,
    val title: String,
    val mimeType: String,
) : Parcelable {
    companion object {
        private const val ENTRY_OBJ_KEY = "entryObj"
        private const val PATH_KEY = "path"
        private const val TITLE_KEY = "title"
        private const val MIME_TYPE_KEY = "mimeType"

        /**
         * returns the LocalPreviewArgs obj
         */
        fun with(args: Bundle): LocalPreviewArgs {
            if (args.containsKey(ENTRY_OBJ_KEY)) {
                val entry = args.getParcelable(ENTRY_OBJ_KEY) as Entry?
                return LocalPreviewArgs(
                    entry,
                    path = entry?.path ?: "",
                    title = entry?.name ?: "",
                    mimeType = entry?.mimeType ?: "",
                )
            } else {
                return LocalPreviewArgs(
                    path = args.getString(PATH_KEY, ""),
                    title = args.getString(TITLE_KEY, ""),
                    mimeType = args.getString(MIME_TYPE_KEY, ""),
                )
            }
        }
    }
}

/**
 * Mark as LocalPreviewFragment
 */
class LocalPreviewFragment : Fragment(), MavericksView {
    private lateinit var args: LocalPreviewArgs

    @OptIn(InternalMavericksApi::class)
    private val viewModel: LocalPreviewViewModel by fragmentViewModelWithArgs { args }
    private lateinit var binding: FragmentLocalPreviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLocalPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args = LocalPreviewArgs.with(requireArguments())
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        configureViewer(args)
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater,
    ) {
        withState(viewModel) { state ->
            when (state.entry?.uploadServer) {
                UploadServerType.UPLOAD_TO_TASK -> {
                    inflater.inflate(R.menu.menu_preview, menu)
                }
                else -> {}
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.download -> {
                viewModel.execute()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun configureViewer(argsLocal: LocalPreviewArgs) {
//        requireActivity().invalidateOptionsMenu()
        binding.title.text = argsLocal.title
        val type = MimeType.with(argsLocal.mimeType)
        binding.icon.setImageDrawable(
            ResourcesCompat.getDrawable(resources, type.icon, requireContext().theme),
        )

        val fragment = createViewer(argsLocal.mimeType)
        if (fragment != null) {
            binding.apply {
                info.isVisible = fragment.showInfoWhenLoaded() == true
                status.text = ""
            }
            val childArgs = ChildViewerArgs(argsLocal.path, argsLocal.mimeType)
            fragment.arguments = bundleOf(Mavericks.KEY_ARG to childArgs)

            childFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment, tag)
                .commit()
        } else {
            binding.apply {
                info.isVisible = true
                status.text = getString(R.string.error_preview_not_available)
            }
        }
    }

    private fun createViewer(mimeType: String) =
        when {
            MediaPreviewProvider.isMimeTypeSupported(mimeType) -> MediaPreviewProvider
            ImagePreviewProvider.isMimeTypeSupported(mimeType) -> ImagePreviewProvider
            PdfPreviewProvider.isMimeTypeSupported(mimeType) -> PdfPreviewProvider
            TextPreviewProvider.isMimeTypeSupported(mimeType) -> TextPreviewProvider
            else -> null
        }?.createViewer()

    override fun invalidate() {
    }
}
