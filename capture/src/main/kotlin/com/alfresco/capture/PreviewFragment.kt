package com.alfresco.capture

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.alfresco.capture.databinding.FragmentPreviewBinding
import com.alfresco.ui.WindowCompat
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import kotlinx.parcelize.Parcelize

@Parcelize
data class PreviewArgs(
    val path: String
) : Parcelable {
    companion object {
        private const val PATH_KEY = "path"

        fun with(args: Bundle): PreviewArgs {
            return PreviewArgs(
                args.getString(PATH_KEY, "")
            )
        }

        fun bundle(path: String) = bundleOf(PATH_KEY to path)
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

        binding.imageView.apply {
            orientation = SubsamplingScaleImageView.ORIENTATION_USE_EXIF
            setImage(ImageSource.uri(args.path))
        }

        binding.closeButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

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
