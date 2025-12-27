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

    fun setCameraResolution(width: Int, height: Int) {
        cameraWidth = width
        cameraHeight = height
    }

    private var modelWidth: Int = 0
    private var modelHeight: Int = 0
    fun setModelResolution(width: Int, height: Int) {
        modelWidth = width
        modelHeight = height
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

    private fun getClassName(det: Detection): String {
        return assetService.labels?.getOrNull(det.classIndex) ?: "\"${det.classIndex}\""
    }

    private var scaledRect: RectF = RectF()
    private var bgRect: RectF = RectF()
    private var rawRect: RectF = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (cameraWidth == 0 || cameraHeight == 0) throw IllegalStateException("Camera width or height was not set for OverlayView")

        val scale = max(
            width.toFloat() / cameraWidth.toFloat(),
            height.toFloat() / cameraHeight.toFloat()
        )

        val cropX = (cameraWidth * scale - width) / 2f
        val cropY = (cameraHeight * scale - height) / 2f

        detections.forEach { det ->
            rawRect = det.toRectF()

            val fixedLeft = (rawRect.left - letterboxInfo.padX) / letterboxInfo.scale
            val fixedTop = (rawRect.top - letterboxInfo.padY) / letterboxInfo.scale
            val fixedRight = (rawRect.right - letterboxInfo.padX) / letterboxInfo.scale
            val fixedBottom = (rawRect.bottom - letterboxInfo.padY) / letterboxInfo.scale

            scaledRect.set(
                fixedLeft * scale - cropX,
                fixedTop * scale - cropY,
                fixedRight * scale - cropX,
                fixedBottom * scale - cropY
            )

            canvas.drawRect(scaledRect, boxPaint)

            val label = "${getClassName(det)}: ${(det.score * 100).toInt()}%"

            val textWidth = textPaint.measureText(label)
            val textHeight = textPaint.fontMetrics.run { bottom - top }

            // filled rectangle for text
            bgRect.left = scaledRect.left
            bgRect.top = scaledRect.top - textHeight - 8
            bgRect.right = scaledRect.left + textWidth + 16
            bgRect.bottom = scaledRect.top

            canvas.drawRect(bgRect, textBackgroundPaint)

            canvas.drawText(label, scaledRect.left + 8, scaledRect.top - 8, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            detections.firstOrNull { it.toRectF().contains(event.x, event.y) }?.let { onDetectionClicked(it) }
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