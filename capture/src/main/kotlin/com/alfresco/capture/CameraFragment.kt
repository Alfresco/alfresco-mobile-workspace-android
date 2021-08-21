package com.alfresco.capture

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.FlashMode
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.AlfrescoCameraController
import androidx.camera.view.CameraController
import androidx.camera.view.video.ExperimentalVideo
import androidx.camera.view.video.OnVideoSavedCallback
import androidx.camera.view.video.OutputFileOptions
import androidx.camera.view.video.OutputFileResults
import androidx.core.app.ActivityCompat
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
import com.alfresco.content.data.LocationData
import com.alfresco.ui.KeyHandler
import com.alfresco.ui.WindowCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.launch

@OptIn(ExperimentalVideo::class)
class CameraFragment : Fragment(), KeyHandler, MavericksView {

    private val viewModel: CaptureViewModel by activityViewModel()

    private lateinit var layout: CameraLayout
    private var discardPhotoDialog = WeakReference<AlertDialog>(null)
    private val locationData: LocationData by lazy {
        LocationData(requireContext())
    }

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraController: AlfrescoCameraController? = null

    /** Blocking camera operations are performed using this executor */
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

        layout.modeSelectorView.setMode(viewModel.mode)
        configureShutterButton(viewModel.mode)
    }

    override fun onPause() {
        super.onPause()

        // Exit fullscreen
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
        inflater.inflate(R.layout.fragment_camera, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layout = view as CameraLayout

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Prepare UI controls
        setUpCameraUi()

        layout.preview.setOnClickListener {
            navigateToSave()
        }
    }

    private suspend fun updateCameraState() {
        if (PermissionFragment.requestPermissions(
                requireContext(),
                CaptureHelperFragment.requiredPermissions(),
                CaptureHelperFragment.permissionRationale(requireContext())
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
            layout.messageView.text = resources.getString(R.string.capture_failure_permissions)
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
            lensFacing = when {
                hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }

            // Enable or disable switching between cameras
            updateCameraSwitchButton()

            // Setup camera controller
            configureCamera()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun configureCamera() {
        layout.aspectRatio = viewModel.mode.aspectRatio().toFloat()

        cameraController = AlfrescoCameraController(requireContext()).apply {
            setEnabledUseCases(useCaseFor(viewModel.mode))
            setCameraSelector(lensFacing)
            imageCaptureFlashMode = DEFAULT_FLASH_MODE
        }.also {
            it.bindToLifecycle(this)
            it.initializationFuture.addListener({
                // Update flash button when ready
                updateFlashControlState()
            }, ContextCompat.getMainExecutor(requireContext()))
        }

        layout.viewFinder.controller = cameraController

        // Observe zoom changes
        cameraController?.zoomState?.observe(this) {
            layout.zoomTextView.text = String.format("%.1f\u00D7", it.zoomRatio)
        }
    }

    private fun useCaseFor(mode: CaptureMode) =
        when (mode) {
            CaptureMode.Photo -> CameraController.IMAGE_CAPTURE
            CaptureMode.Video -> CameraController.VIDEO_CAPTURE
        }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP -> {
                // When the volume down button is pressed, simulate a shutter button click
                layout.shutterButton.simulateClick()
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
                onTakePhotoButtonClick(controller)
            } else if (controller.isVideoCaptureEnabled) {
                onTakeVideoButtonClick(controller)
            }
        }

        layout.modeSelectorView.onMode = { mode ->
            viewModel.mode = mode
            configureShutterButton(mode)
            configureCamera()
        }

        // Setup for button used to switch cameras
        layout.cameraSwitchButton.setOnClickListener {
            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            cameraController?.setCameraSelector(lensFacing)
            cameraController?.imageCaptureFlashMode = DEFAULT_FLASH_MODE
            updateFlashControlState()
            layout.animateCameraSwitchClick()
        }

        layout.flashButton.setOnClickListener {
            showFlashMenu()
        }

        layout.closeButton.setOnClickListener {
            withState(viewModel) {
                if (it.listCapture.isNotEmpty())
                    discardPhotoPrompt(it.listCapture.size)
                else
                    requireActivity().finish()
            }
        }
    }

    private fun onTakePhotoButtonClick(controller: CameraController) {
        // Create output file to hold the image
        val photoFile = viewModel.prepareCaptureFile(viewModel.mode)

        Logger.d("Photo capture succeeded before: ${photoFile.name}")

        // Create output options object which contains file + metadata

        val outputOptions = when {
            LocationUtils.isLocationEnabled(requireActivity()) -> {
                ImageCapture.OutputFileOptions.Builder(photoFile)
                    .setMetadata(viewModel.getMetaData()).build()
            }
            else -> {
                ImageCapture.OutputFileOptions.Builder(photoFile).build()
            }
        }

        // Setup image capture listener which is triggered after photo has been taken
        controller.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                    Logger.d("Photo capture succeeded: $savedUri")
                    viewModel.onCapturePhoto(savedUri)
                }

                override fun onError(exc: ImageCaptureException) {
                    Logger.e("Photo capture failed: ${exc.message}", exc)
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

    private fun onTakeVideoButtonClick(controller: CameraController) {
        if (controller.isRecording) {
            layout.shutterButton.state = ShutterButton.State.Video
            layout.modeSelectorView.isVisible = true
            layout.captureDurationView.isVisible = false
            layout.cameraSwitchButton.isVisible = true

            controller.stopRecording()
        } else {
            layout.shutterButton.state = ShutterButton.State.Recording
            layout.modeSelectorView.isVisible = false
            layout.captureDurationView.isVisible = true
            layout.cameraSwitchButton.isVisible = false

            val videoFile = viewModel.prepareCaptureFile(viewModel.mode)
            val outputOptions = OutputFileOptions.builder(videoFile).build()

            cameraController?.startRecording(
                outputOptions,
                cameraExecutor,
                object : OnVideoSavedCallback {
                    override fun onVideoSaved(output: OutputFileResults) {
                        val savedUri = output.savedUri ?: Uri.fromFile(videoFile)
                        Logger.d("Video capture succeeded: $savedUri")
                        viewModel.onCaptureVideo(savedUri)
                    }

                    override fun onError(
                        videoCaptureError: Int,
                        message: String,
                        cause: Throwable?
                    ) {
                        Logger.e("Video capture failed: ${cause?.message}", cause)
                    }
                })
        }
    }

    private fun configureShutterButton(mode: CaptureMode) {
        layout.shutterButton.state = when (mode) {
            CaptureMode.Photo -> ShutterButton.State.Photo
            CaptureMode.Video -> ShutterButton.State.Video
        }
    }

    private fun navigateToSave() {
        view?.post {
            findNavController().navigate(R.id.action_cameraFragment_to_saveFragment)
        }
    }

    private fun showFlashMenu() {
        layout.flashMenu.isVisible = true
        layout.flashMenu.onMenuItemClick = { mode ->
            val flashMode = when (mode) {
                FlashMenuItem.On -> ImageCapture.FLASH_MODE_ON
                FlashMenuItem.Off -> ImageCapture.FLASH_MODE_OFF
                FlashMenuItem.Auto -> ImageCapture.FLASH_MODE_AUTO
            }

            cameraController?.imageCaptureFlashMode = flashMode
            layout.flashButton.setImageResource(flashModeIcon(flashMode))

            // Hide menu
            layout.flashMenu.isVisible = false
        }
    }

    private fun updateCameraSwitchButton() {
        layout.cameraSwitchButton.isVisible =
            try {
                hasBackCamera() && hasFrontCamera()
            } catch (exception: CameraInfoUnavailableException) {
                false
            }
    }

    /** Called when camera changes. */
    private fun updateFlashControlState() {
        layout.flashButton.isVisible = flashControlEnabled(viewModel.mode, cameraController)
        layout.flashMenu.isVisible = false

        val flashMode = cameraController?.imageCaptureFlashMode ?: ImageCapture.FLASH_MODE_AUTO
        layout.flashButton.setImageResource(flashModeIcon(flashMode))
    }

    private fun flashControlEnabled(mode: CaptureMode, controller: AlfrescoCameraController?) =
        when (mode) {
            CaptureMode.Photo -> controller?.hasFlashUnit() ?: false
            CaptureMode.Video -> false
        }

    private fun flashModeIcon(@FlashMode flashMode: Int) =
        when (flashMode) {
            ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
            ImageCapture.FLASH_MODE_OFF -> R.drawable.ic_flash_off
            else -> R.drawable.ic_flash_auto
        }

    private fun hasBackCamera(): Boolean =
        cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false

    private fun hasFrontCamera(): Boolean =
        cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false

    override fun invalidate(): Unit = withState(viewModel) {
        if (it.listCapture.isNotEmpty()) {
            layout.preview.load(it.listCapture.last()?.uri, imageLoader)
            layout.imageCount.text = it.listCapture.size.toString()
            layout.rlPreview.visibility = View.VISIBLE
        } else {
            layout.imageCount.text = ""
            layout.rlPreview.visibility = View.GONE
        }
    }

    private fun discardPhotoPrompt(count: Int) {
        val oldDialog = discardPhotoDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.discard_title))
            .setMessage("$count " + resources.getString(R.string.discard_subtitle))
            .setNegativeButton(resources.getString(R.string.discard_confirmation_negative), null)
            .setPositiveButton(resources.getString(R.string.discard_confirmation_positive)) { _, _ ->
                viewModel.clearCaptures()
                requireActivity().finish()
            }
            .show()
        discardPhotoDialog = WeakReference(dialog)
    }

    override fun onStart() {
        super.onStart()
        invokeLocation()
    }

    private fun invokeLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            when {
                LocationUtils.isLocationEnabled(requireActivity()) -> {
                    locationData.observe(this, {
                        viewModel.longitude = it.longitude.toString()
                        viewModel.latitude = it.latitude.toString()
                    })
                }
            }
        }
    }

    companion object {
        private val TAG: String = CameraFragment::class.java.simpleName
        private const val DEFAULT_FLASH_MODE = ImageCapture.FLASH_MODE_AUTO
    }
}
