package cv.cbglib.commonUI

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import cv.cbglib.detection.Detection
import cv.cbglib.detection.LetterboxInfo
import cv.demoapps.bangdemo.MyApp
import cv.demoapps.bangdemo.R
import kotlin.math.max

class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val detections = mutableListOf<Detection>()
    private var letterboxInfo = LetterboxInfo(1f, 0, 0)

    private val assetService =
        (context.applicationContext as MyApp).assetService

    private var cameraWidth: Int = 0
    private var cameraHeight: Int = 0
    private var scale: Float = 1f
    private var cropX: Float = 0f
    private var cropY: Float = 0f

    fun setCameraResolution(width: Int, height: Int) {
        cameraWidth = width
        cameraHeight = height

        scale = max(
            this.width.toFloat() / cameraWidth.toFloat(),
            this.height.toFloat() / cameraHeight.toFloat()
        )

        cropX = (cameraWidth * scale - this.width) / 2f
        cropY = (cameraHeight * scale - this.height) / 2f
    }

    private val boxPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.detection_box)
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    private val textBackgroundPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.detection_text_background)
        style = Paint.Style.FILL
        alpha = 180
    }

    private val textPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.detection_text)
        textSize = 32f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        isAntiAlias = true
    }

    private val errorTextPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.detection_text)
        textSize = 64f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        isAntiAlias = true
    }

    private fun getClassName(det: Detection): String {
        return assetService.labels?.getOrNull(det.classIndex) ?: "\"${det.classIndex}\""
    }

    private var tmpRect: RectF = RectF()
    private var scaledRect: RectF = RectF()
    private var bgRect: RectF = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (cameraWidth == 0 || cameraHeight == 0) {
            writeErrorOnScreen(canvas, "Camera width or height was not set for OverlayView")
        } else {
            detections.forEach { det ->
                scaledRect = scaleDetectionToScreenRect(det)

                canvas.drawRect(tmpRect, boxPaint)

                val label = "${getClassName(det)}: ${(det.score * 100).toInt()}%"

                val textWidth = textPaint.measureText(label)
                val textHeight = textPaint.fontMetrics.run { bottom - top }

                // filled rectangle for text
                bgRect.left = tmpRect.left
                bgRect.top = tmpRect.top - textHeight - 8
                bgRect.right = tmpRect.left + textWidth + 16
                bgRect.bottom = tmpRect.top

                canvas.drawRect(bgRect, textBackgroundPaint)

                canvas.drawText(label, tmpRect.left + 8, tmpRect.top - 8, textPaint)
            }
        }
    }

    private fun writeErrorOnScreen(canvas: Canvas, text: String) {
        canvas.drawText(text, 0f, height / 2f, errorTextPaint)
    }

    private fun scaleDetectionToScreenRect(det: Detection): RectF {
        tmpRect = det.toRectF()

        val fixedLeft = (tmpRect.left - letterboxInfo.padX) / letterboxInfo.scale
        val fixedTop = (tmpRect.top - letterboxInfo.padY) / letterboxInfo.scale
        val fixedRight = (tmpRect.right - letterboxInfo.padX) / letterboxInfo.scale
        val fixedBottom = (tmpRect.bottom - letterboxInfo.padY) / letterboxInfo.scale

        tmpRect.set(
            fixedLeft * scale - cropX,
            fixedTop * scale - cropY,
            fixedRight * scale - cropX,
            fixedBottom * scale - cropY
        )

        return tmpRect
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            detections.firstOrNull { scaleDetectionToScreenRect(it).contains(event.x, event.y) }
                ?.let { onDetectionClicked(it) }
        }

        return true
    }

    fun onDetectionClicked(item: Detection) {
        AlertDialog.Builder(context)
            .setTitle("Clicked on ${getClassName(item)}")
            .setMessage("Clicked on ${getClassName(item)} because you clicked")
            .setPositiveButton("Yes", null)
            .show()
    }

    fun updateBoxes(newBoxes: List<Detection>, letterboxInfo: LetterboxInfo) {
        detections.clear()
        detections.addAll(newBoxes)
        this.letterboxInfo = letterboxInfo
        invalidate()
    }
}