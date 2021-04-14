package com.alfresco.capture

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.FlashMode
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.AlfrescoCameraController
import androidx.camera.view.CameraController
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.alfresco.content.PermissionFragment
import com.alfresco.ui.KeyHandler
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.launch

class CameraFragment : Fragment(), KeyHandler, MavericksView {

    private val viewModel: CaptureViewModel by activityViewModel()

    private lateinit var layout: CameraLayout
    private lateinit var outputDirectory: File

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraController: AlfrescoCameraController? = null

    @FlashMode
    private var flashMode: Int = ImageCapture.FLASH_MODE_AUTO

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    override fun onResume() {
        super.onResume()

        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        lifecycleScope.launch {
            updateCameraState()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Shut down our background executor
        cameraExecutor.shutdown()
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

        // Determine the output directory
        outputDirectory = getOutputDirectory(requireContext())

        // Prepare UI controls
        setUpCameraUi()
    }

    private suspend fun updateCameraState() {
        if (PermissionFragment.requestPermission(requireContext(), Manifest.permission.CAMERA)) {
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

            // Show/hide flash if available
            updateFlashModeButton()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun configureCamera() {
        // TODO: figure out preview aspect ratio
        layout.aspectRatio = 4 / 3f

        cameraController = AlfrescoCameraController(requireContext()).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            setCameraSelector(lensFacing)
        }.also {
            it.bindToLifecycle(this)
        }

        layout.viewFinder.controller = cameraController
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
            // Create output file to hold the image
            val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)

            // Create output options object which contains file + metadata
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .build()

            // Setup image capture listener which is triggered after photo has been taken
            cameraController?.takePicture(
                outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                        Log.d(TAG, "Photo capture succeeded: $savedUri")
                        viewModel.capturePhoto(photoFile.toString())
                        navigateToSave()
                    }
                })

            // Display flash animation to indicate that photo was captured
            layout.postDelayed({
                layout.foreground = ColorDrawable(Color.WHITE)
                layout.postDelayed(
                    { layout.foreground = null }, ANIMATION_FAST_MILLIS
                )
            }, ANIMATION_SLOW_MILLIS)
        }

        // Setup for button used to switch cameras
        layout.cameraSwitchButton.setOnClickListener {
            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            cameraController?.setCameraSelector(lensFacing)
            updateFlashModeButton()
            animateCameraSwitchClick()
        }

        layout.flashButton.setOnClickListener {
            showFlashMenu(it)
        }

        layout.closeButton.setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun animateCameraSwitchClick() {
        layout.cameraSwitchButton.let {
            val fromValue = it.rotation
            it.animate()
                .rotationBy(-360f)
                .withEndAction { it.rotation = fromValue }
                .start()
        }
    }

    private fun navigateToSave() {
        view?.post {
            findNavController().navigate(R.id.action_cameraFragment_to_saveFragment)
        }
    }

    private fun showFlashMenu(v: View) {
        val popup = PopupMenu(requireContext(), v)
        popup.inflate(R.menu.flash_mode)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popup.setForceShowIcon(true)
        }

        popup.setOnMenuItemClickListener {
            flashMode = when (it.itemId) {
                R.id.flash_mode_on -> ImageCapture.FLASH_MODE_ON
                R.id.flash_mode_off -> ImageCapture.FLASH_MODE_OFF
                R.id.flash_mode_auto -> ImageCapture.FLASH_MODE_AUTO
                else -> ImageCapture.FLASH_MODE_AUTO
            }

            cameraController?.imageCaptureFlashMode = flashMode
            updateFlashModeButton()
            true
        }

        popup.show()
    }

    private fun updateCameraSwitchButton() {
        layout.cameraSwitchButton.isVisible =
            try {
                hasBackCamera() && hasFrontCamera()
            } catch (exception: CameraInfoUnavailableException) {
                false
            }
    }

    private fun updateFlashModeButton() {
        layout.flashButton.isVisible = cameraController?.hasFlashUnit() ?: false

        val iconRes = when (flashMode) {
            ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
            ImageCapture.FLASH_MODE_OFF -> R.drawable.ic_flash_off
            else -> R.drawable.ic_flash_auto
        }
        layout.flashButton.setImageResource(iconRes)
    }

    private fun hasBackCamera(): Boolean =
        cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false

    private fun hasFrontCamera(): Boolean =
        cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false

    companion object {

        private const val TAG = "CameraX"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"

        /** Helper function used to create a timestamped file */
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(baseFolder, SimpleDateFormat(format, Locale.US)
                .format(System.currentTimeMillis()) + extension)

        /** Use external media if it is available, our app's file directory otherwise */
        // TODO: Figure out a proper output directory
        private fun getOutputDirectory(context: Context): File =
            context.applicationContext.cacheDir
    }

    override fun invalidate() {
        // no-op
    }
}