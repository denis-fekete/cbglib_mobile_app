package cv.cbglib.detection

import android.content.Context
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.concurrent.futures.await
import androidx.lifecycle.LifecycleOwner
import cv.cbglib.detection.detectors.realtime.YoloONNXDetector
import cv.cbglib.logging.PerformanceLogOverlay
import cv.demoapps.bangdemo.MyApp
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Class making camera control abstracted. Creates new thread on which a [RealTimeAnalyzer] is run.
 *
 * @param context Should be a context of a [android.app.Fragment] or [android.app.Activity], in case either of these are
 * destroyed, new camera controller along with [ExecutorService] will be created.
 * @param lifecycleOwner Owner of lifecycle, used by CameraX to correctly bind.
 * @param previewView [PreviewView] that is in layout where this [CameraController] is situated,
 * this preview shows unedited stream of images from camera (in another word video from camera).
 * @param overlayView Class used for drawing, it is expected that the class will be subclassed.
 *
 * Function [stop] must be called, otherwise a detached thread might cause memory errors.
 */
class CameraController(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val overlayView: OverlayView,
    private val performanceLogOverlay: PerformanceLogOverlay
) {
    private var cameraControllerInitialized: Boolean = false
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var preview: Preview
    private lateinit var realtimeAnalyzer: RealTimeAnalyzer

    private val assetService by lazy {
        (context.applicationContext as MyApp).assetService
    }

    private val settingsService by lazy {
        (context.applicationContext as MyApp).settingsService
    }

    /**
     * Returns [ByteArray] of model from [cv.cbglib.services.SettingsService]. Models must be stored inside
     * `assets/models/`.
     */
    private fun getModelBytes(): ByteArray? {
        try {
            return assetService.getModel(settingsService.selectedModel, "models/")

        } catch (exc: IOException) {
            if (assetService.availableModels.isNotEmpty()) {
                AlertDialog.Builder(context)
                    .setTitle("Error")
                    .setMessage("Model could not be loaded, trying to load first available. Error message: ${exc.message}")
                    .setPositiveButton("OK", null)
                    .show()

                return assetService.getModel(assetService.availableModels.first(), "models/")
            } else {
                AlertDialog.Builder(context)
                    .setTitle("Error")
                    .setMessage(
                        "No models found, please check \'assets/models\' directory to contain models with correct extensions." +
                                " Error message: ${exc.message}"
                    )
                    .setPositiveButton("OK", null)
                    .show()

                return null
            }
        }
    }

    private fun getResolutionSelector(): ResolutionSelector {
        // minimal size with ration 16:9, fewer pixels, less accurate but, more performance
        return ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy(
                    Size(640, 480),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                )
            )
            .setAspectRatioStrategy(
                AspectRatioStrategy(
                    AspectRatio.RATIO_16_9,
                    AspectRatioStrategy.FALLBACK_RULE_AUTO
                )
            )
            .build()
    }

    /**
     * Initializes all camera and image analysis related options.
     * Source [source](https://developer.android.com/media/camera/camerax/analyze#operating_modes)
     */
    suspend fun start() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraControllerInitialized = true

        cameraProvider = ProcessCameraProvider.getInstance(context).await()

        val modelByteArray = getModelBytes() ?: return

        realtimeAnalyzer = RealTimeAnalyzer(
            settingsService.framesToSkip,
            overlayView,
            performanceLogOverlay,
            YoloONNXDetector(
                modelByteArray,
                settingsService.showPerformance,
                settingsService.verbosePerformance
            )
        )

        val resolutionSelector = getResolutionSelector()

        // keep only latest, if image analyzer is not keeping up (calculations take too much time), then keep only the
        // most recent image instead of buffering them
        val imageAnalysis = ImageAnalysis.Builder()
            .setOutputImageRotationEnabled(true)
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor, realtimeAnalyzer)

        preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .build()
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
            AlertDialog.Builder(context)
                .setTitle("Info")
                .setMessage("Exception during camera initialization: ${exc.message}")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    /**
     * Destroys [cameraExecutor] that is running on different thread, to prevent memory leaks must be called!
     */
    fun stop() {
        if (cameraControllerInitialized)
            cameraExecutor.shutdown()

        realtimeAnalyzer.destroy()
    }
}
