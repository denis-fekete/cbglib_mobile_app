package cv.cbglib.detection

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.util.Log
import androidx.camera.core.ImageProxy
import cv.cbglib.logging.PerformanceLogOverlay
import cv.cbglib.logging.PerformanceLogValue
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.nio.FloatBuffer

/**
 * Implementation of abstract [BaseImageAnalyzer] for ONNX runtime environment using YOLO models.
 *
 * @param framesToSkip Number of frames that will be skipped == only every `N`th frame will be analyzed.
 * @param overlayView Derived class from [OverlayView], to which analyzer will store detections.
 * @param performanceLogOverlay Overlay for displaying performance logs, if null not logs will be displayed.
 * @param showPerformanceLogging Whenever logging should be displayed.
 * @param verbosePerformanceLogging Whenever a verbose (more detailed) logs should be displayed.
 */
class ImageAnalyzerONNX(
    modelBytes: ByteArray,
    private val framesToSkip: Int = 5,
    private val overlayView: OverlayView,
    private val performanceLogOverlay: PerformanceLogOverlay? = null,
    private val showPerformanceLogging: Boolean,
    private val verbosePerformanceLogging: Boolean,
) : BaseImageAnalyzer() {
    private var ortSession: OrtSession
    private var ortEnvironment: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var inputName: String

    /**
     * Shared function for analyzing the image, whenever performance logging will be enabled or not depends on
     * [performanceLogOverlay] and [verbosePerformanceLogging].
     */
    private var analyzeFunction: (ImageProxy) -> Unit

    init {
        // try to use Nnapi for hardware accelerated detection, on fail use CPU
        try {
            val sessionOptions = OrtSession.SessionOptions()
            sessionOptions.addNnapi()
            ortSession = ortEnvironment.createSession(modelBytes, sessionOptions)
            Log.i(javaClass.simpleName, "Loaded NNAPI for OnnxRuntime")
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Failed to use NNAPI for OnnxRuntime, using CPU instead", e)
            ortSession = ortEnvironment.createSession(modelBytes)
        }

        inputName = ortSession.inputNames.first()

        analyzeFunction = if (showPerformanceLogging && performanceLogOverlay != null) {
            if (verbosePerformanceLogging) {
                ::verboseMetricsAnalyze
            } else {
                ::metricsAnalyze
            }
        } else {
            ::noMetricsAnalyze
        }
    }

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

        analyzeFunction(imageProxy)
    }

    /**
     * Analyze function with verbose performance logging
     */
    private fun verboseMetricsAnalyze(imageProxy: ImageProxy) {
        val (_, timeBitmap) = measureTime {
            Utils.bitmapToMat(
                imageProxy.toBitmap(),
                bitmapMat
            )
        }

        // close imageProxy so buffers can be reused
        imageProxy.close()

        if (bitmapMat.empty())
            return

        // resize image into expected size for model, apply letterboxing if needed
        val (letterBoxInfo, timeLetterboxing) = measureTime {
            resizeAndLetterBox(bitmapMat, modelInputWidth, letterBoxMat)
        }
        // create tensor from Mat
        val (tensor, timeTensor) = measureTime { matToTensor(letterBoxMat) }

        // run model on tensor, and get result
        val (results, timeDetection) = measureTime { ortSession.run(mapOf(inputName to tensor)) }

        // convert flat outputs into an 3D array
        val result3D = results[0].value as Array<Array<FloatArray>> // [batch, values, detections]

        // extract bounding boxes [Detection] objects from results that
        val (detections, timeExtractDetections) = measureTime { extractDetections(result3D, 0.6f) }

        // apply NMS onto results
        val (filteredDetections, timeNMS) = measureTime { applyNMS(detections, 0.6f, 0.5f) }

        val performanceLogList = listOf(
            PerformanceLogValue("Bitmap", timeBitmap),
            PerformanceLogValue("LetterBox", timeLetterboxing),
            PerformanceLogValue("Tensor", timeTensor),
            PerformanceLogValue("Detection", timeDetection),
            PerformanceLogValue("Extract detections", timeExtractDetections),
            PerformanceLogValue("NMS", timeNMS)
        )

        performanceLogOverlay?.post {
            performanceLogOverlay.updateLogData(performanceLogList)
        }
        // add new [Detection] boxes to draw and invalidate View that is drawing them
        overlayView.post {
            overlayView.updateBoxes(filteredDetections, letterBoxInfo)
        }

        results.close()
        tensor.close()
    }

    /**
     * Analyze function with basic (total time) performance logging
     */
    private fun metricsAnalyze(imageProxy: ImageProxy) {
        val (_, totalTime) = measureTime { noMetricsAnalyze(imageProxy) }

        performanceLogOverlay?.post {
            performanceLogOverlay.updateLogData(
                listOf(PerformanceLogValue("", totalTime)) // empty key will not be shown
            )
        }
    }

    /**
     * Analyze function with no performance logging
     */
    private fun noMetricsAnalyze(imageProxy: ImageProxy) {
        Utils.bitmapToMat(
            imageProxy.toBitmap(),
            bitmapMat
        )

        // close imageProxy so buffers can be reused
        imageProxy.close()

        if (bitmapMat.empty())
            return

        // resize image into expected size for model, apply letterboxing if needed
        val letterBoxInfo = resizeAndLetterBox(bitmapMat, modelInputWidth, letterBoxMat)
        // create tensor from Mat
        val tensor = matToTensor(letterBoxMat)

        // run model on tensor, and get result
        val results = ortSession.run(mapOf(inputName to tensor))

        // convert flat outputs into an 3D array
        val result3D = results[0].value as Array<Array<FloatArray>> // [batch, values, detections]

        // extract bounding boxes [Detection] objects from results that
        val detections = extractDetections(result3D, 0.6f)

        // apply NMS onto results
        val filteredDetections = applyNMS(detections, 0.6f, 0.5f)

        // add new [Detection] boxes to draw and invalidate View that is drawing them
        overlayView.post {
            overlayView.updateBoxes(filteredDetections, letterBoxInfo)
        }

        results.close()
        tensor.close()
    }

    /**
     * Converts OpenCV Mat containing input image into an OnnxTensor that can be put into OnnxSession for object
     * detection. OpenCV uses HWC format, where the ONNX expects and CHW format, for that and image has to converted.
     *
     * @return [OnnxTensor] that can be put as an input to OnnxRuntime model
     */
    private fun matToTensor(mat: Mat): OnnxTensor {
        // convert from RGB Alpha into RGB
        Imgproc.cvtColor(mat, rgbMat, Imgproc.COLOR_RGBA2RGB)

        // convert RGB to normalized values <0-1>
        rgbMat.convertTo(floatMat, CvType.CV_32FC3, 1.0 / 255.0)

        // separate channels
        val channels = ArrayList<Mat>()
        Core.split(floatMat, channels)

        // onnx model expects an CHW format in its tensor, CHW: [(R1, R2, R3, ...), [G1, G2, ...], [B1, ... )]
        val height = mat.rows()
        val width = mat.cols()
        val chw = FloatArray(3 * height * width)

        for (i in 0..2) {
            val channelData = FloatArray(height * width)
            channels[i].get(0, 0, channelData)
            System.arraycopy(channelData, 0, chw, i * height * width, height * width)
        }

        // batch size, channels, heigh, width == batch size of 1 for CHW image
        val shape = longArrayOf(1, 3, height.toLong(), width.toLong())

        // create tensor, wrap used as a View, no copying, better performance
        return OnnxTensor.createTensor(ortEnvironment, FloatBuffer.wrap(chw), shape)
    }
}