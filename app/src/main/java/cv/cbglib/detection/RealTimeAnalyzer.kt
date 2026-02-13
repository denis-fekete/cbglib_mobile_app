package cv.cbglib.detection

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import cv.cbglib.logging.PerformanceLogOverlay

class RealTimeAnalyzer(
    private val framesToSkip: Int = 5,
    private val overlayView: OverlayView,
    private val performanceLogOverlay: PerformanceLogOverlay? = null,
    private val detector: IDetector,
) : ImageAnalysis.Analyzer {

    private var skippedFramesCounter: Int = Int.MAX_VALUE // activate at first frame
    private var resolutionInitialized = false

    /**
     * Function that gets called by CameraProvider to analyze the current image. The [imageProxy] is output of a camera
     * and is an in buffers, these should be freed as fast as possible.
     */
    override fun analyze(imageProxy: ImageProxy) {
        if (skippedFramesCounter++ < framesToSkip) {
            imageProxy.close()
            return
        }

        skippedFramesCounter = 0

        if (!resolutionInitialized) {
            overlayView.setCameraResolution(imageProxy.width, imageProxy.height)
            resolutionInitialized = true
        }

        val result = detector.detect(imageProxy)

        if (result.metrics != null) {
            performanceLogOverlay?.post {
                performanceLogOverlay.updateLogData(result.metrics)
            }
        }

        // add new [Detection] boxes to draw and invalidate View that is drawing them
        overlayView.post {
            overlayView.updateBoxes(result.detections, result.details)
        }
    }

    fun destroy() {
        detector.destroy()
    }
}