package cv.cbglib.detection

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import cv.cbglib.commonUI.OverlayView
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfInt
import org.opencv.core.MatOfRect2d
import org.opencv.core.Rect2d
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.dnn.Dnn
import org.opencv.imgproc.Imgproc
import kotlin.math.max
import kotlin.math.roundToInt

abstract class BaseImageAnalyzer(
) : ImageAnalysis.Analyzer {
    /**
     * Number of frames to skip, not every frame of camera, needs to be checked, 0 means every
     */
    protected var framesToSkip = 5

    protected var skippedFramesCounter: Int = Int.MAX_VALUE - framesToSkip - 1
    protected var resolutionInitialized = false
    protected val modelInputWidth = 640
    //    val modelInputHeight = 640 // not used since model expects 1:1 ratio of images

    // "cache" variables to prevent initializing new object each new frame
    protected var bitmapMat = Mat()
    protected var resized = Mat()
    protected var letterBoxMat = Mat()
    protected var rgbMat = Mat()
    protected var floatMat = Mat()

    /**
     * Resized [src] Mat into a size that model can use. If source Mat is not in 1:1 aspect ratio a letterbox is
     * applied to make it into desired size in 1:1 ratio. [newSize] is desired size and [padValue] is color value
     * that will be used for padding.
     * @param src source Mat containing image
     * @param newSize new size of pixels, since 1:1 is expected only one dimension is needed
     * @param outputMap Output Mat where output of this operation will be stored
     * @param padValue color used for padding
     *
     * @return LetterboxInfo letterboxed
     */
    protected fun resizeAndLetterBox(
        src: Mat,
        newSize: Int,
        outputMap: Mat,
        padValue: Scalar = Scalar(114.0, 114.0, 114.0),
    ): LetterboxInfo {
        // find bigger dimension (width / height)
        val srcW = src.cols()
        val srcH = src.rows()
        val scale = newSize.toFloat() / max(srcW, srcH)

        // new image size (width and height)
        val newW = (srcW * scale).roundToInt()
        val newH = (srcH * scale).roundToInt()

        Imgproc.resize(src, resized, Size(newW.toDouble(), newH.toDouble()))

        // calculate padding, padded image is always centered
        val padX = (newSize - newW) / 2
        val padY = (newSize - newH) / 2

        // copies resized and apply border/letterboxing
        Core.copyMakeBorder(
            resized,
            outputMap,
            padY,
            newSize - newH - padY,
            padX,
            newSize - newW - padX,
            Core.BORDER_CONSTANT,
            padValue
        )

        return LetterboxInfo(scale, padX, padY)
    }

    protected fun transpose(output: Array<FloatArray>): Array<FloatArray> {
        val rows = output.size
        val cols = output[0].size
        val transposed = Array(cols) { FloatArray(rows) }
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                transposed[j][i] = output[i][j]
            }
        }
        return transposed
    }

    /**
     * Extracts list of [Detection] objects from OrtSession result.
     * Results are in format `[batch, values, detections]` where the values are:
     * x, y, w, h, class0 confidence, class1 confidence, class2 confidence...
     *
     * @return List of [Detection] that pass the [confThreshold] confidence score threshold
     */
    protected fun extractDetections(results: Array<Array<FloatArray>>, confThreshold: Float = 0.6f): List<Detection> {
        // remove batch dimension as model only outputs one batch
        val rawDetections = results[0] // [values, detections]

        // transpose from [values, detections] into more user friendly [detections, values]
        val transposedDetections = transpose(rawDetections)

        val detections = mutableListOf<Detection>()

        for (value in transposedDetections) {
            val classScores = value.sliceArray(4 until value.size)

            var bestScore = -Float.MAX_VALUE
            var bestClass = -1

            for (i in classScores.indices) {
                val score = classScores[i]
                if (score > bestScore) {
                    bestScore = score
                    bestClass = i
                }
            }

            if (bestScore < confThreshold)
                continue

            val x = value[0]
            val y = value[1]
            val w = value[2]
            val h = value[3]

            detections.add(Detection(x, y, w, h, bestClass, bestScore))
        }

        return detections
    }

    /**
     * Apply Non-Maximum Suppression (NMS) on list of [Detection]s. Implemented using OpenCV NSM function.
     */
    protected fun applyNMS(
        detections: List<Detection>,
        confThreshold: Float,
        iouThreshold: Float
    ): List<Detection> {
        if (detections.isEmpty()) return emptyList()

        val finalDetections = mutableListOf<Detection>()

        // group detections by class
        val detectionsByClass = detections.groupBy { it.classIndex }

        for ((classId, classDetections) in detectionsByClass) {
            if (classDetections.isEmpty()) continue

            // bounding boxes
            val rect2dArr: Array<Rect2d> = Array(classDetections.size) { i ->
                classDetections[i].toRect2d()
            }
            val matRects = MatOfRect2d(*rect2dArr)

            // scores
            val scores = FloatArray(classDetections.size) { i -> classDetections[i].score }
            val matScores = MatOfFloat(*scores)

            // run NMS for this class
            val matIndices = MatOfInt()
            Dnn.NMSBoxes(
                matRects,
                matScores,
                confThreshold,
                iouThreshold,
                matIndices
            )

            // collect results
            for (idx in matIndices.toArray()) {
                finalDetections.add(classDetections[idx])
            }

            // release native resources
            matRects.release()
            matScores.release()
            matIndices.release()
        }

        return finalDetections
    }

    protected inline fun <T> measureTime(action: () -> T): Pair<T, Long> {
        val start = SystemClock.elapsedRealtimeNanos()
        val result = action()
        val end = SystemClock.elapsedRealtimeNanos()

        return result to (end - start)
    }

    /**
     * Clean up variables that are used as "cache" (not actual cache but frequently used where reallocation each frame
     * does not make sense).
     */
    public fun destroy() {
        bitmapMat.release()
        resized.release()
        letterBoxMat.release()
        rgbMat.release()
        floatMat.release()
    }
}