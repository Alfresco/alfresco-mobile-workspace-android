package com.alfresco.scan

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
import com.google.android.material.snackbar.Snackbar
import com.zynksoftware.documentscanner.R
import com.zynksoftware.documentscanner.common.extensions.scaledBitmap
import com.zynksoftware.documentscanner.common.utils.OpenCvNativeBridge
import com.zynksoftware.documentscanner.model.DocumentScannerErrorModel
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Marked as CropFragment class
 */
class CropFragment : Fragment(), MavericksView {

    private val viewModel: ScanViewModel by activityViewModel()
    private lateinit var binding: FragmentCropBinding

    private val nativeClass = OpenCvNativeBridge()

    private var selectedImage: Bitmap? = null

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
            val image = getCroppedImage()

            image?.let { imageBitmap ->
                var fileOutputStream: FileOutputStream? = null
                try {
                    fileOutputStream = FileOutputStream(destFile, true)
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    fileOutputStream?.close()
                }

                viewModel.onCapturePhotoOrPdf(Uri.fromFile(destFile), true)

                requireActivity().onBackPressed()
            }
        }

        binding.btnRetake.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun invalidate() = withState(viewModel) {
    }

    private fun initializeCropping() {
        if (selectedImage != null && selectedImage!!.width > 0 && selectedImage!!.height > 0) {
            val scaledBitmap: Bitmap = selectedImage!!.scaledBitmap(binding.holderImageCrop.width, binding.holderImageCrop.height)
            binding.imagePreview.setImageBitmap(scaledBitmap)
            val tempBitmap = (binding.imagePreview.drawable as BitmapDrawable).bitmap
            val pointFs = getEdgePoints(tempBitmap)
            Log.d(TAG, "ZDCgetEdgePoints ends ${System.currentTimeMillis()}")
            binding.polygonView.setPoints(pointFs)
            binding.polygonView.visibility = View.VISIBLE
            val padding = resources.getDimension(R.dimen.zdc_polygon_dimens).toInt()
            val layoutParams = FrameLayout.LayoutParams(tempBitmap.width + padding, tempBitmap.height + padding)
            layoutParams.gravity = Gravity.CENTER
            binding.polygonView.layoutParams = layoutParams
        }
    }

    private fun getEdgePoints(tempBitmap: Bitmap): Map<Int, PointF> {
        Log.d(TAG, "ZDCgetEdgePoints Starts ${System.currentTimeMillis()}")
        val pointFs: List<PointF> = nativeClass.getContourEdgePoints(tempBitmap)
        return binding.polygonView.getOrderedValidEdgePoints(tempBitmap, pointFs)
    }

    private fun getCroppedImage(): Bitmap? {
        if (selectedImage != null) {
            try {
                Log.d(TAG, "ZDCgetCroppedImage starts ${System.currentTimeMillis()}")
                val points: Map<Int, PointF> = binding.polygonView.getPoints()
                val xRatio: Float = selectedImage!!.width.toFloat() / binding.imagePreview.width
                val yRatio: Float = selectedImage!!.height.toFloat() / binding.imagePreview.height
                val pointPadding = requireContext().resources.getDimension(R.dimen.zdc_point_padding).toInt()
                val x1: Float = (points.getValue(0).x + pointPadding) * xRatio
                val x2: Float = (points.getValue(1).x + pointPadding) * xRatio
                val x3: Float = (points.getValue(2).x + pointPadding) * xRatio
                val x4: Float = (points.getValue(3).x + pointPadding) * xRatio
                val y1: Float = (points.getValue(0).y + pointPadding) * yRatio
                val y2: Float = (points.getValue(1).y + pointPadding) * yRatio
                val y3: Float = (points.getValue(2).y + pointPadding) * yRatio
                val y4: Float = (points.getValue(3).y + pointPadding) * yRatio
                val croppedImage = nativeClass.getScannedBitmap(selectedImage!!, listOf(x1, y1, x2, y2, x3, y3, x4, y4))
                Log.d(TAG, "ZDCgetCroppedImage ends ${System.currentTimeMillis()}")
                return croppedImage
            } catch (e: java.lang.Exception) {
                Log.e(TAG, DocumentScannerErrorModel.ErrorMessage.CROPPING_FAILED.error, e)
                Snackbar.make(binding.btnKeepScan, DocumentScannerErrorModel.ErrorMessage.CROPPING_FAILED.error, Snackbar.LENGTH_SHORT).show()
            }
        } else {
            Log.e(TAG, DocumentScannerErrorModel.ErrorMessage.INVALID_IMAGE.error)
            Snackbar.make(binding.btnKeepScan, DocumentScannerErrorModel.ErrorMessage.INVALID_IMAGE.error, Snackbar.LENGTH_SHORT).show()
        }
        return null
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

                    selectedImage = bitmapDrawable.bitmap

                    binding.holderImageView.post {
                        initializeCropping()
                    }
                }
            })
            .build()
    }
    companion object {
        private val TAG = CropFragment::class.simpleName
    }
}
