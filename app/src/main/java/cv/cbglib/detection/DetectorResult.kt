package cv.cbglib.detection

import cv.cbglib.logging.PerformanceLogValue

data class DetectorResult(
    val detections: List<Detection>,
    val details: ImageDetails,
    val metrics: List<PerformanceLogValue>?
)
