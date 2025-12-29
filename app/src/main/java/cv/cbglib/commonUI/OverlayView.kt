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

abstract class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    protected val detections = mutableListOf<Detection>()
    private var letterboxInfo = LetterboxInfo(1f, 0, 0)

    private val assetService =
        (context.applicationContext as MyApp).assetService

    private var cameraWidth: Int = 0
    private var cameraHeight: Int = 0
    private var scale: Float = 1f
    private var cropX: Float = 0f
    private var cropY: Float = 0f

    var onDetectionClicked: ((detection: Detection) -> Unit)? = null

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

    private val errorTextPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.detection_text)
        textSize = 48f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        isAntiAlias = true
    }

    protected fun getClassName(det: Detection): String {
        return assetService.labels?.getOrElse(
            det.classIndex
        ) { "\"${det.classIndex}\"" }
            .toString()
    }

    private var tmpRect: RectF = RectF()


    protected fun writeErrorOnScreen(canvas: Canvas, text: String) {
        canvas.drawText(text, 0f, height / 2f, errorTextPaint)
    }

    protected fun cameraDimensionsCorrect(): Boolean {
        return (cameraWidth > 0 && cameraHeight > 0)
    }

    protected fun scaleDetectionToScreenRect(det: Detection): RectF {
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
                ?.let { onDetectionClicked?.invoke(it) }
        }

        return true
    }

    fun updateBoxes(newBoxes: List<Detection>, letterboxInfo: LetterboxInfo) {
        detections.clear()
        detections.addAll(newBoxes)
        this.letterboxInfo = letterboxInfo
        invalidate()
    }
}