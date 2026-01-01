package cv.demoapps.bangdemo.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import cv.cbglib.commonUI.OverlayView
import cv.demoapps.bangdemo.MyApp
import cv.demoapps.bangdemo.R

class BangOverlayView(context: Context, attrs: AttributeSet?) : OverlayView(context, attrs) {
    private var scaledRect: RectF = RectF()
    private var bgRect: RectF = RectF()

    private val cardDetailsService =
        (context.applicationContext as MyApp).cardDetailsService

    private val boxPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.detection_box)
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
        alpha = 200
    }

    private val textBackgroundPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.detection_text_background)
        style = Paint.Style.FILL
        alpha = 200
    }

    private val textPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.detection_text)
        textSize = 32f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        if (!cameraDimensionsCorrect()) {
            writeErrorOnScreen(canvas, "Camera is loading")
        } else {
            detections.forEach { det ->
                scaledRect = scaleDetectionToScreenRect(det)

                canvas.drawRect(scaledRect, boxPaint)

                val label = "${cardDetailsService.items[det.classIndex]?.name}: ${(det.score * 100).toInt()}%"
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
    }
}