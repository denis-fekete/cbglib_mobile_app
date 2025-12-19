package cv.cbglib.detection

import android.content.Context
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.concurrent.futures.await
import androidx.lifecycle.LifecycleOwner
import cv.cbglib.commonUI.OverlayView
import cv.demoapps.bangdemo.MyApp
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraController(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val overlayView: OverlayView,
) {
    private var initCalled: Boolean = false
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var preview: Preview

    private val assetService =
        (context.applicationContext as MyApp).assetService

    fun init() {
        initCalled = true

        // crearte cameraExecutor on new thread, required by CameraX
        cameraExecutor = Executors.newSingleThreadExecutor()
    }


    /**
     * Start camera on
     * Source [source](https://developer.android.com/media/camera/camerax/analyze#operating_modes)
     */
    suspend fun startCamera() {
        if (!initCalled) throw IllegalStateException("CameraController.init() was not called")

        cameraProvider = ProcessCameraProvider.getInstance(context).await()


        val imageAnalyzer = ImageAnalyzer(assetService.modelByteArray, overlayView)

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(640, 640))
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor, imageAnalyzer)

        preview = Preview.Builder().build()
        preview.surfaceProvider = previewView.surfaceProvider

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                imageAnalysis,
                preview
            )

        } catch (exc: Exception) {

        }
    }

    fun destroy() {
        cameraExecutor.shutdown()
    }
}
