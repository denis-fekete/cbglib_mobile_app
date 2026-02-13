package cv.cbglib.detection.detectors.realtime

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.util.Log
import androidx.camera.core.ImageProxy
import cv.cbglib.detection.Detection
import cv.cbglib.detection.DetectorResult
import cv.cbglib.detection.IDetector
import cv.cbglib.logging.PerformanceLogValue
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.nio.FloatBuffer

class YoloONNXDetector(
    modelBytes: ByteArray,
    private val showPerformanceLogging: Boolean,
    private val verbosePerformanceLogging: Boolean,
) : AbstractYoloDetector(), IDetector {
    private var ortSession: OrtSession
    private var ortEnvironment: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var inputName: String
    private val modelInputWidth = 640
    //    val modelInputHeight = 640 // not used since model expects 1:1 ratio of images

    private var metricsList = mutableListOf<PerformanceLogValue>()

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
    }

    override fun detect(imageProxy: ImageProxy): DetectorResult {
        metricsList.clear()

        val (detections, totalTime) = measureTime { verboseMetricsAnalysis(imageProxy) }

        metricsList.add(PerformanceLogValue("Total", totalTime))

        return if (showPerformanceLogging) {
            DetectorResult(detections, imageDetails, metricsList)
        } else {
            DetectorResult(detections, imageDetails, null)
        }
    }

    /**
     * Analyze function with verbose performance logging
     */
    private fun verboseMetricsAnalysis(imageProxy: ImageProxy): List<Detection> {
        // convert ImageProxy => Bitmap => OpenCV.Mat
        val (_, timeBitmap) = measureTime {
            Utils.bitmapToMat(
                imageProxy.toBitmap(),
                bitmapMat
            )
        }

        // close imageProxy so buffers can be reused
        imageProxy.close()

        // resize image into expected size for model, apply letterboxing if needed
        val (letterBoxMat, timeLetterboxing) = measureTime {
            resizeAndLetterBox(bitmapMat, modelInputWidth)
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

        if (showPerformanceLogging && verbosePerformanceLogging) {
            metricsList.add(PerformanceLogValue("Bitmap", timeBitmap))
            metricsList.add(PerformanceLogValue("LetterBox", timeLetterboxing))
            metricsList.add(PerformanceLogValue("Tensor", timeTensor))
            metricsList.add(PerformanceLogValue("Detection", timeDetection))
            metricsList.add(PerformanceLogValue("Extract detections", timeExtractDetections))
            metricsList.add(PerformanceLogValue("NMS", timeNMS))
        }

        results.close()
        tensor.close()

        return filteredDetections
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

    override fun destroy() {
        super.cleanup()
    }
}