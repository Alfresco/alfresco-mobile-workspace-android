package com.alfresco.scan

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.EventListener
import coil.ImageLoader
import coil.fetch.VideoFrameFileFetcher
import coil.load
import coil.request.ImageRequest
import coil.request.ImageResult
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.scan.databinding.FragmentCropBinding
import com.labters.documentscanner.getCroppedImage
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Marked as CropFragment class
 */
class CropFragment : Fragment(), MavericksView {

    private val viewModel: ScanViewModel by activityViewModel()
    private lateinit var binding: FragmentCropBinding
    private val File_CROP_DIR = "crop"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCropBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loadingAnimation.visibility = View.VISIBLE

        binding.sd.load(viewModel.uri, imageLoader)

        binding.btnKeepScan.setOnClickListener {
            val destFile = viewModel.prepareCropFile()
            val image = binding.documentScanner.getCroppedImage()

            var fileOutputStream: FileOutputStream? = null
            try {
                fileOutputStream = FileOutputStream(destFile, true)
                image.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                fileOutputStream?.close()
            }

            viewModel.onCapturePhoto(Uri.fromFile(destFile))

            requireActivity().onBackPressed()
        }

        binding.btnRetake.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun invalidate() = withState(viewModel) {
    }

    private val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(requireContext())
            .componentRegistry {
                add(VideoFrameFileFetcher(requireContext()))
            }
            .eventListener(object : EventListener {
                override fun onSuccess(request: ImageRequest, metadata: ImageResult.Metadata) {
                    super.onSuccess(request, metadata)

                    binding.loadingAnimation.visibility = View.GONE

                    val bitmapDrawable = binding.sd.drawable as BitmapDrawable

                    val bitmap = bitmapDrawable.bitmap

                    binding.documentScanner.setImage(bitmap)
                }
            })
            .build()
    }
}
