package com.alfresco.capture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.Mavericks
import com.alfresco.capture.databinding.FragmentPreviewBinding
import com.alfresco.content.viewer.common.ChildViewerArgs
import com.alfresco.content.viewer.image.ImagePreviewProvider
import com.alfresco.content.viewer.media.MediaPreviewProvider
import com.alfresco.ui.WindowCompat
import java.lang.IllegalArgumentException

data class PreviewArgs(
    val path: String,
    val mimeType: String
) {
    companion object {
        private const val PATH_KEY = "path"
        private const val MIME_TYPE_KEY = "mimeType"

        fun with(args: Bundle): PreviewArgs {
            return PreviewArgs(
                args.getString(PATH_KEY, ""),
                args.getString(MIME_TYPE_KEY, "")
            )
        }

        fun bundle(path: String, mimeType: String) =
            bundleOf(PATH_KEY to path, MIME_TYPE_KEY to mimeType)
    }
}

class PreviewFragment : Fragment() {

    private lateinit var binding: FragmentPreviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = PreviewArgs.with(requireArguments())
        configureViewer(args)
    }

    private fun configureViewer(args: PreviewArgs) {
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

    override fun onResume() {
        super.onResume()

        setFullscreen(true)
    }

    override fun onPause() {
        super.onPause()

        setFullscreen(false)
    }

    private fun setFullscreen(fullscreen: Boolean) {
        activity?.window?.let {
            if (fullscreen) {
                WindowCompat.enterImmersiveMode(it)
            } else {
                WindowCompat.restoreSystemUi(it)
            }
        }
    }
}
