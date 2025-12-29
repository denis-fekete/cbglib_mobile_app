package cv.cbglib.detection

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
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
import java.nio.FloatBuffer
import kotlin.math.max
import kotlin.math.roundToInt

class ImageAnalyzer(
    modelBytes: ByteArray,
    private val overlayView: OverlayView,
) : ImageAnalysis.Analyzer {
    var ortSession: OrtSession
    var ortEnvironment: OrtEnvironment = OrtEnvironment.getEnvironment()
    var inputName: String
    var resolutionInitialized = false

    val modelInputWidth = 640
    val modelInputHeight = 640
    var bitmapMat = Mat()
    private var skippedFramesCounter = 0

    init {
        // try to use Nnapi for hardware accelerated detection, on fail use CPU
        try {
            val sessionOptions = OrtSession.SessionOptions()
            sessionOptions.addNnapi()
            ortSession = ortEnvironment.createSession(modelBytes, sessionOptions)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Failed to use NNAPI for OnnxRuntime, using CPU instead", e)
            ortSession = ortEnvironment.createSession(modelBytes)
        }

        inputName = ortSession.inputNames.first()
    }

    /**
     * Function that gets called by CameraProvider to analyze the current image. The [imageProxy] is output of a camera
     * and is an in buffers, these should be freed as fast as possible.
     */
    override fun analyze(imageProxy: ImageProxy) {
        if (skippedFramesCounter++ > 5) {
            skippedFramesCounter = 0

            if (!resolutionInitialized) {
                overlayView.setCameraResolution(imageProxy.width, imageProxy.height)
                resolutionInitialized = true
            }

            Utils.bitmapToMat(
                imageProxy.toBitmap(),
                bitmapMat
            )

            imageProxy.close() // close so buffers can be reused

            val (letterBoxMat, letterBoxInfo) = resizeAndLetterBox(bitmapMat, modelInputWidth)

            val tensor = matToTensor(letterBoxMat)

            val results = ortSession.run(mapOf(inputName to tensor))

            val detections = extractDetections(results)

            val filteredDetections = applyNMS(detections, 0.6f, 0.5f)

            overlayView.post {
                overlayView.updateBoxes(filteredDetections, letterBoxInfo)
            }

            results.close()
        } else {
            imageProxy.close()
        }
    }

    /**
     * Resized [src] Mat into a size that model can use. If source Mat is not in 1:1 aspect ratio a letterbox is
     * applied to make it into desired size in 1:1 ratio. [newSize] is desired size and [padValue] is color value
     * that will be used for padding.
     */
    fun resizeAndLetterBox(
        src: Mat,
        newSize: Int,
        padValue: Scalar = Scalar(114.0, 114.0, 114.0)
    ):
            Pair<Mat, LetterboxInfo> {
        val srcW = src.cols()
        val srcH = src.rows()


        val scale = newSize.toFloat() / max(srcW, srcH)
        val newW = (srcW * scale).roundToInt()
        val newH = (srcH * scale).roundToInt()

        val resized = Mat()
        Imgproc.resize(src, resized, Size(newW.toDouble(), newH.toDouble()))

        val padX = (newSize - newW) / 2
        val padY = (newSize - newH) / 2

        val output = Mat()

        Core.copyMakeBorder(
            resized,
            output,
            padY,
            newSize - newH - padY,
            padX,
            newSize - newW - padX,
            Core.BORDER_CONSTANT,
            padValue
        )
        resized.release()

        return output to LetterboxInfo(scale, padX, padY)
    }

    /**
     * Converts OpenCV Mat containing input image into an OnnxTensor that can be put into OnnxSession for object
     * detection. OpenCV uses HWC format, where the ONNX expects and CHW format, for that and image has to converted.
     *
     * @return [OnnxTensor] that can be put as an input to OnnxRuntime model
     */
    private fun matToTensor(mat: Mat): OnnxTensor {
        // convert from RGB Alpha into RGB
        val rgbMat = Mat()
        Imgproc.cvtColor(mat, rgbMat, Imgproc.COLOR_RGBA2RGB)

        // convert RGB to normalized values <0-1>
        val floatMat = Mat()
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

    private fun transpose(output: Array<FloatArray>): Array<FloatArray> {
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
     * x, y, w, h, class0 confidence, class1 confidence, class2 ...
     *
     * @return List of [Detection] that pass the [confThreshold] confidence score threshold
     */
    private fun extractDetections(results: OrtSession.Result, confThreshold: Float = 0.6f): List<Detection> {
        // convert flat outputs into an 3D array
        val result3D = results[0].value as Array<Array<FloatArray>> // [batch, values, detections]
        // remove batch dimension as model only outputs one batch
        val rawDetections = result3D[0] // [values, detections]

        // transpose from [values, detections] into more user friendly [detections, values]
        val transposedDetections = transpose(rawDetections)

        val detections = mutableListOf<Detection>()

        for (value in transposedDetections) {
            val class0Conf = value[4]
            val class1Conf = value[5]
            val (classIndex, score) = if (class0Conf > class1Conf) 0 to class0Conf else 1 to class1Conf

            if (score < confThreshold)
                continue

            val x = value[0]
            val y = value[1]
            val w = value[2]
            val h = value[3]

            detections.add(Detection(x, y, w, h, score, classIndex))
        }

        return detections
    }

    /**
     * Apply Non-Maximum Suppression (NMS) on list of [Detection]s. Implemented using OpenCV NSM function.
     */
    private fun applyNMS(
        detections: List<Detection>,
        confThreshold: Float = 0.5f,
        iouThreshold: Float = 0.45f,
    ): List<Detection> {
        if (detections.isEmpty()) return emptyList()

        // get mat bounding boxes
        val rect2dArr: Array<Rect2d> = Array(detections.size) { i ->
            detections[i].toRect2d()
        }
        val matRects = MatOfRect2d(*rect2dArr)

        // get mat scores
        val scoresArr = FloatArray(detections.size) { i -> detections[i].score }
        val matScores = MatOfFloat(*scoresArr)

        // run opencv NMS
        val matIndices = MatOfInt()
        Dnn.NMSBoxes(matRects, matScores, confThreshold.toFloat(), iouThreshold.toFloat(), matIndices)

        // filter detections based on indices
        val indicesArr = matIndices.toArray()
        val filtered = indicesArr.map { detections[it] }

        matRects.release()
        matScores.release()
        matIndices.release()

        return filtered
    }
}