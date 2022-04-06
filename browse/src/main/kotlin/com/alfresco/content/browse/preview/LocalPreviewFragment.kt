package com.alfresco.content.browse.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.Mavericks
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentLocalPreviewBinding
import com.alfresco.content.mimetype.MimeType
import com.alfresco.content.viewer.common.ChildViewerArgs
import com.alfresco.content.viewer.image.ImagePreviewProvider
import com.alfresco.content.viewer.media.MediaPreviewProvider
import com.alfresco.content.viewer.pdf.PdfPreviewProvider
import com.alfresco.content.viewer.text.TextPreviewProvider

/**
 * Mark as LocalPreviewArgs
 */
data class LocalPreviewArgs(
    val path: String,
    val title: String,
    val mimeType: String
) {
    companion object {
        private const val PATH_KEY = "path"
        private const val TITLE_KEY = "title"
        private const val MIME_TYPE_KEY = "mimeType"

        /**
         * returns the LocalPreviewArgs obj
         */
        fun with(args: Bundle): LocalPreviewArgs {
            return LocalPreviewArgs(
                args.getString(PATH_KEY, ""),
                args.getString(TITLE_KEY, ""),
                args.getString(MIME_TYPE_KEY, "")
            )
        }
    }
}

/**
 * Mark as LocalPreviewFragment
 */
class LocalPreviewFragment : Fragment() {

    private lateinit var binding: FragmentLocalPreviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocalPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = LocalPreviewArgs.with(requireArguments())
        configureViewer(args)
    }

    private fun configureViewer(argsLocal: LocalPreviewArgs) {
        binding.title.text = argsLocal.title
        val type = MimeType.with(argsLocal.mimeType)
        binding.icon.setImageDrawable(
            ResourcesCompat.getDrawable(resources, type.icon, requireContext().theme)
        )

        println("mime type ${argsLocal.mimeType}")

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
}
