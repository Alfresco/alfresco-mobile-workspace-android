package com.alfresco.content.viewer

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.alfresco.content.data.BrowseRepository
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import kotlinx.coroutines.launch

class ImageViewerFragment(
    private val documentId: String,
    private val mimeType: String
) : Fragment(R.layout.viewer_image) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Stream data and improve performance
        val imageView = view.findViewById<SubsamplingScaleImageView>(R.id.imageView)
        lifecycleScope.launch {
            val stream = BrowseRepository().fetchContentStream(documentId)
            imageView.setBitmapDecoderFactory(InputStreamImageDecoder.Factory(stream))
            imageView.setRegionDecoderFactory(InputStreamImageRegionDecoder.Factory(stream))
            imageView.setImage(ImageSource.asset("dummy"))
        }
    }
}
