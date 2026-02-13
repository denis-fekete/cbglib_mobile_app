package cv.cbglib.detection

import androidx.camera.core.ImageProxy

interface IDetector {
    /**
     * Runs image detection analysis and returns [DetectorResult] containing detections, image information and
     * optionally metrics.
     */
    fun detect(imageProxy: ImageProxy): DetectorResult
    fun destroy()
}