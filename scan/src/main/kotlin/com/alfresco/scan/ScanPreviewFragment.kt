package com.alfresco.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.Mavericks
import com.alfresco.content.viewer.common.ChildViewerArgs
import com.alfresco.content.viewer.image.ImagePreviewProvider
import com.alfresco.content.viewer.media.MediaPreviewProvider
import com.alfresco.scan.databinding.FragmentScanPreviewBinding

/**
 * Marked as ScanPreviewArgs class
 */
data class ScanPreviewArgs(
    val path: String,
    val mimeType: String
) {
    companion object {
        private const val PATH_KEY = "path"
        private const val MIME_TYPE_KEY = "mimeType"

        /**
         * returns the ScanPreviewArgs after adding the data from arguments
         */
        fun with(args: Bundle): ScanPreviewArgs {
            return ScanPreviewArgs(
                args.getString(PATH_KEY, ""),
                args.getString(MIME_TYPE_KEY, "")
            )
        }

        /**
         * returns the bundle which contains the path
         */
        fun bundle(path: String, mimeType: String) =
            bundleOf(PATH_KEY to path, MIME_TYPE_KEY to mimeType)
    }
}

/**
 * Marked as ScanPreviewFragment class
 */
class ScanPreviewFragment : Fragment() {

    private lateinit var binding: FragmentScanPreviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = ScanPreviewArgs.with(requireArguments())
        configureViewer(args)
    }

    private fun configureViewer(args: ScanPreviewArgs) {
        val fragment = createViewer(args.mimeType)
        val childArgs = ChildViewerArgs(args.path, args.mimeType)
        fragment.arguments = bundleOf(Mavericks.KEY_ARG to childArgs)

        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment, tag)
            .commit()
    }

    private fun createViewer(mimeType: String) =
        when {
            MediaPreviewProvider.isMimeTypeSupported(mimeType) -> MediaPreviewProvider
            ImagePreviewProvider.isMimeTypeSupported(mimeType) -> ImagePreviewProvider
            else -> throw IllegalArgumentException("Unsupported MIME type")
        }.createViewer()
}
