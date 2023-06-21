package androidx.camera.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.MainThread
import androidx.annotation.OptIn
import androidx.annotation.RequiresPermission
import androidx.annotation.RestrictTo
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.LensFacing
import androidx.camera.core.impl.utils.Threads
import androidx.lifecycle.LifecycleOwner
import com.alfresco.Logger

@SuppressLint("RestrictedApi")
class AlfrescoCameraController(context: Context) :
    CameraController(context) {
    private var mLifecycleOwner: LifecycleOwner? = null

    /**
     * Sets the [LifecycleOwner] to be bound with the controller.
     *
     *
     *  The state of the lifecycle will determine when the cameras are open, started, stopped
     * and closed. When the [LifecycleOwner]'s state is start or greater, the controller
     * receives camera data. It stops once the [LifecycleOwner] is destroyed.
     *
     * @throws IllegalStateException If the provided camera selector is unable to resolve a
     * camera to be used for the given use cases.
     * @see ProcessCameraProvider.bindToLifecycle
     */
    @SuppressLint("MissingPermission")
    @MainThread
    fun bindToLifecycle(lifecycleOwner: LifecycleOwner) {
        Threads.checkMainThread()
        mLifecycleOwner = lifecycleOwner
        startCameraAndTrackStates()
    }

    /**
     * Clears the previously set [LifecycleOwner] and stops the camera.
     *
     * @see ProcessCameraProvider.unbindAll
     */
    @MainThread
    fun unbind() {
        Threads.checkMainThread()
        mLifecycleOwner = null
        mCamera = null
        if (mCameraProvider != null) {
            mCameraProvider!!.unbindAll()
        }
    }

    /**
     * Unbind and rebind all use cases to [LifecycleOwner].
     *
     * @return null if failed to start camera.
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    override fun startCamera(): Camera? {
        if (mLifecycleOwner == null) {
            Logger.d("Lifecycle is not set.")
            return null
        }
        if (mCameraProvider == null) {
            Logger.d("CameraProvider is not ready.")
            return null
        }
        val useCaseGroup = createUseCaseGroup() // Use cases can't be created.
            ?: return null
        return mCameraProvider!!.bindToLifecycle(mLifecycleOwner!!, mCameraSelector, useCaseGroup)
    }

    fun setCameraSelector(@LensFacing lensFacing: Int) {
        this.cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
    }

    fun hasFlashUnit() =
        mCamera?.cameraInfo?.hasFlashUnit() == true

    /**
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.TESTS)
    fun shutDownForTests() {
        if (mCameraProvider != null) {
            mCameraProvider!!.unbindAll()
            mCameraProvider!!.shutdown()
        }
    }

    companion object {
        private const val TAG = "CamLifecycleController"
    }
}
