package com.alfresco.scan

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.FlashMode
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.AlfrescoScanController
import androidx.camera.view.CameraController
import androidx.camera.view.video.ExperimentalVideo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import coil.fetch.VideoFrameFileFetcher
import coil.load
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.Logger
import com.alfresco.content.PermissionFragment
import com.alfresco.ui.ScanKeyHandler
import com.alfresco.ui.ScanWindowCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.launch

/**
 * Marked as ScanFragment class
 */
@OptIn(ExperimentalVideo::class)
class ScanFragment : Fragment(), ScanKeyHandler, MavericksView {

    private val viewModel: ScanViewModel by activityViewModel()

    private lateinit var layout: ScanLayout
    private var discardPhotoDialog = WeakReference<AlertDialog>(null)

    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraController: AlfrescoScanController? = null

    private lateinit var cameraExecutor: ExecutorService

    private val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(requireContext())
            .componentRegistry {
                add(VideoFrameFileFetcher(requireContext()))
            }
            .build()
    }

    override fun onResume() {
        super.onResume()
        // Enter fullscreen
        setFullscreen(true)

        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        lifecycleScope.launch {
            updateCameraState()
        }

        layout.shutterButton.state = ShutterButton.State.Photo
    }

    override fun onPause() {
        super.onPause()

        // Exit fullscreen
        setFullscreen(false)
    }

    private fun setFullscreen(fullscreen: Boolean) {
        activity?.window?.let {
            if (fullscreen) {
                ScanWindowCompat.enterImmersiveMode(it)
            } else {
                ScanWindowCompat.restoreSystemUi(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Shut down our background executor
        cameraExecutor.shutdown()

        // Invalidate controller
        cameraController = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_scan, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layout = view as ScanLayout

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Prepare UI controls
        setUpCameraUi()

        layout.preview.setOnClickListener {
            viewModel.createPdf()
        }

        viewModel.onPdfCreated = {
            view.post {
                findNavController().navigate(R.id.action_scanFragment_to_scanPreviewFragment)
            }
        }
    }

    private suspend fun updateCameraState() {
        if (PermissionFragment.requestPermissions(
                requireContext(),
                ScanHelperFragment.requiredPermissions(),
                ScanHelperFragment.permissionRationale(requireContext())
            )
        ) {
            if (cameraController == null) {
                setUpCamera()
            }
            layout.messageView.isVisible = false
            layout.viewFinder.isVisible = true
        } else {
            if (cameraController != null) {
                layout.viewFinder.controller = null
                cameraController = null
            }
            layout.messageView.text = resources.getString(R.string.scan_permissions_rationale)
            layout.messageView.isVisible = true
            layout.viewFinder.isVisible = false
        }
    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {

            // CameraProvider
            cameraProvider = cameraProviderFuture.get()

            // Select lensFacing depending on the available cameras
            if (viewModel.lensFacing == -1)
                viewModel.lensFacing = when {
                    hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                    else -> throw IllegalStateException("Back camera is unavailable")
                }

            // Enable or disable switching between cameras
            layout.cameraSwitchButton.visibility = View.INVISIBLE

            // Setup camera controller
            configureCamera()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun configureCamera() {
        layout.aspectRatio = ScanMode.Photo.aspectRatio().toFloat()

        cameraController = AlfrescoScanController(requireContext()).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            setCameraSelector(viewModel.lensFacing)
            imageCaptureFlashMode = viewModel.flashMode
        }.also {
            it.bindToLifecycle(this)
            it.initializationFuture.addListener({
                // Update flash button when ready
                updateScanFlashControlState()
            }, ContextCompat.getMainExecutor(requireContext()))
        }

        layout.viewFinder.controller = cameraController

        // Observe zoom changes
        cameraController?.zoomState?.observe(viewLifecycleOwner) {
            layout.zoomTextView.text = String.format("%.1f\u00D7", it.zoomRatio)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP -> {
                // When the volume down button is pressed, simulate a shutter button click
                layout.shutterButton.simulateClick()
                true
            }
            KeyEvent.KEYCODE_BACK -> {
                goBack()
                true
            }
            else -> false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpCameraUi() {
        // Show focus overlay on tap
        layout.viewFinder.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_UP -> {
                    val f = layout.focusView
                    f.alpha = 1f
                    f.x = event.x - f.width / 2
                    f.y = event.y - f.height / 2
                    f.postDelayed({ f.alpha = 0f }, 2000)
                }
            }
            false
        }

        // Listener for button used to capture photo
        layout.shutterButton.setOnClickListener {
            val controller = requireNotNull(cameraController)
            if (controller.isImageCaptureEnabled) {
                enableShutterButton(false)
                onTakePhotoButtonClick(controller)
            } else {
                Logger.d("either image or video is on processing")
            }
        }

        layout.flashButton.setOnClickListener {
            showFlashMenu()
        }

        layout.closeButton.setOnClickListener {
            goBack()
        }
    }

    private fun goBack() {
        withState(viewModel) {
            if (it.listScanCaptures.isNotEmpty())
                discardPhotoPrompt()
            else
                requireActivity().finish()
        }
    }

    private fun onTakePhotoButtonClick(controller: CameraController) {
        // Create output file to hold the image
        val photoFile = viewModel.prepareCaptureFile()

        // Create output options object which contains file + metadata

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Setup image capture listener which is triggered after photo has been taken
        controller.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                    Logger.d("Photo capture succeeded: $savedUri")
                    viewModel.uri = savedUri

                    layout.animatePreviewHide()
                    enableShutterButton(true)
                    requireActivity().runOnUiThread {
                        layout.animatePreview()
                    }
                    navigateToCrop()
                }

                override fun onError(exc: ImageCaptureException) {
                    Logger.e("Photo capture failed: ${exc.message}", exc)
                    enableShutterButton(true)
                }
            })

        // Display flash animation to indicate that photo was captured
        layout.postDelayed({
            layout.viewFinder.foreground = ColorDrawable(Color.BLACK)
            layout.postDelayed(
                { layout.viewFinder.foreground = null }, ANIMATION_FAST_MILLIS
            )
        }, ANIMATION_SLOW_MILLIS)
    }

    private fun enableShutterButton(enabled: Boolean) {
        requireActivity().runOnUiThread {
            layout.shutterButton.isEnabled = enabled
        }
    }

    private fun navigateToCrop() {
        view?.post {
            findNavController().navigate(R.id.action_scanFragment_to_cropFragment)
        }
    }

    private fun showFlashMenu() {
        layout.flashMenu.isVisible = true
        layout.flashMenu.onMenuItemClick = { mode ->
            viewModel.flashMode = when (mode) {
                ScanFlashMenuItem.On -> ImageCapture.FLASH_MODE_ON
                ScanFlashMenuItem.Off -> ImageCapture.FLASH_MODE_OFF
                ScanFlashMenuItem.Auto -> ImageCapture.FLASH_MODE_AUTO
            }

            cameraController?.imageCaptureFlashMode = viewModel.flashMode
            layout.flashButton.setImageResource(flashModeIcon(viewModel.flashMode))

            // Hide menu
            layout.flashMenu.isVisible = false
        }
    }

    private fun updateScanFlashControlState() {
        layout.flashButton.isVisible = flashControlEnabled(cameraController)
        layout.flashMenu.isVisible = false

        val flashMode = cameraController?.imageCaptureFlashMode ?: viewModel.flashMode
        layout.flashButton.setImageResource(flashModeIcon(flashMode))
    }

    private fun flashControlEnabled(controller: AlfrescoScanController?) = controller?.hasFlashUnit() ?: false

    private fun flashModeIcon(@FlashMode flashMode: Int) =
        when (flashMode) {
            ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
            ImageCapture.FLASH_MODE_OFF -> R.drawable.ic_flash_off
            else -> R.drawable.ic_flash_auto
        }

    private fun hasBackCamera(): Boolean =
        cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false

    override fun invalidate(): Unit = withState(viewModel) {
        if (it.listScanCaptures.isNotEmpty()) {
            layout.preview.load(it.listScanCaptures.last().uri, imageLoader)
            layout.imageCount.text = String.format("%,d", it.listScanCaptures.size)
            layout.rlPreview.visibility = View.VISIBLE
        } else {
            layout.imageCount.text = ""
            layout.rlPreview.visibility = View.INVISIBLE
        }
    }

    private fun discardPhotoPrompt() {
        val oldDialog = discardPhotoDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.discard_scan_title))
            .setMessage(resources.getString(R.string.discard_scan_subtitle))
            .setNegativeButton(resources.getString(R.string.discard_scan_confirmation_negative), null)
            .setPositiveButton(resources.getString(R.string.discard_scan_confirmation_positive)) { _, _ ->
                viewModel.clearCaptures()
                requireActivity().finish()
            }
            .show()
        discardPhotoDialog = WeakReference(dialog)
    }

    companion object {
        private val TAG: String = ScanFragment::class.java.simpleName
    }
}
