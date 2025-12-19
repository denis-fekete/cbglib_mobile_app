package cv.cbglib.commonUI

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import cv.cbglib.detection.Detection
import cv.demoapps.bangdemo.MyApp
import cv.demoapps.bangdemo.R

class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val detections = mutableListOf<Detection>()

    private val assetService =
        (context.applicationContext as MyApp).assetService

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


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        detections.forEach { det ->
            val rect = det.toRectF()

            canvas.drawRect(rect, boxPaint)

            val label = "${getClassName(det)}: ${(det.score * 100).toInt()}%"

            val textWidth = textPaint.measureText(label)
            val textHeight = textPaint.fontMetrics.run { bottom - top }

            // filled rectangle for text
            val bgRect = RectF(
                rect.left,
                rect.top - textHeight - 8,
                rect.left + textWidth + 16,
                rect.top
            )

            canvas.drawRect(bgRect, textBackgroundPaint)

            canvas.drawText(label, rect.left + 8, rect.top - 8, textPaint)
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

    fun updateBoxes(newBoxes: List<Detection>) {
        detections.clear()
        detections.addAll(newBoxes)
        invalidate()
    }
}