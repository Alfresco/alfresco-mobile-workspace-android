package com.alfresco.content.viewer.image

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import java.io.File

class ImageViewerFragment(
    private val documentId: String,
    private val uri: String
) : Fragment(R.layout.viewer_image) {

    private lateinit var downloadMonitor: DownloadStateReceiver

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progressIndicator = view.findViewById<ProgressBar>(R.id.progress_indicator)
        val imageView = view.findViewById<SubsamplingScaleImageView>(R.id.imageView)
        imageView.orientation = SubsamplingScaleImageView.ORIENTATION_USE_EXIF

        val path = requireContext().getExternalFilesDir(null)?.path + "/" + documentId
        val renderImage = {
            progressIndicator.visibility = View.GONE
            imageView.setImage(ImageSource.uri(path))
        }

        val download = File(path)
        if (download.exists()) {
            renderImage()
            return
        }

        val dm = ContextCompat.getSystemService(requireContext(), DownloadManager::class.java)
        val request = DownloadManager.Request(Uri.parse(uri))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle(documentId)
        request.setDescription("")
        request.setDestinationInExternalFilesDir(requireContext(), null, documentId)

        val ref = dm?.enqueue(request)

        downloadMonitor = DownloadStateReceiver(ref ?: -1, renderImage)

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        requireContext().registerReceiver(downloadMonitor, filter)
    }

    class DownloadStateReceiver(
        private val downloadId: Long,
        private val onComplete: () -> Unit
    ) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val referenceId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (referenceId == downloadId) {
                onComplete()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (this::downloadMonitor.isInitialized) {
            requireContext().unregisterReceiver(downloadMonitor)
        }
    }
}
