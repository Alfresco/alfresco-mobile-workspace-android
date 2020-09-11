package com.alfresco.content.viewer.image

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.alfresco.content.viewer.common.ContentDownloader
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import java.io.File
import kotlinx.coroutines.launch

class ImageViewerFragment(
    private val documentId: String,
    private val uri: String
) : Fragment(R.layout.viewer_image) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progressIndicator = view.findViewById<ProgressBar>(R.id.progress_indicator)
        val imageView = view.findViewById<SubsamplingScaleImageView>(R.id.imageView)
        imageView.orientation = SubsamplingScaleImageView.ORIENTATION_USE_EXIF

        val output = File(requireContext().cacheDir, "content.tmp")
        val renderImage = {
            progressIndicator.visibility = View.GONE
            imageView.setImage(ImageSource.uri(output.path))
        }

        lifecycleScope.launch {
            ContentDownloader.downloadFileTo(uri, output.path)
            renderImage()
        }
    }
}
