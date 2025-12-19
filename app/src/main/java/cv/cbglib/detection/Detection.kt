package cv.cbglib.detection

import android.graphics.RectF
import org.opencv.core.Rect2d

/**
 *
 */
data class Detection(
    val x: Float, // center x coordinate of bounding box
    val y: Float, // center y coordinate of bounding box
    val width: Float,
    val height: Float,
    val score: Float,
    val classIndex: Int
) {
    fun toRectF(): RectF {
        val wHalf = width / 2.0f
        val hHalf = height / 2.0f
        return RectF(
            x - wHalf,
            y - hHalf,
            x + wHalf,
            y + hHalf
        )
    }

    fun toRect2d(): Rect2d {
        return Rect2d(
            (x - width / 2f).toDouble(),
            (y - height / 2f).toDouble(),
            width.toDouble(),
            height.toDouble()
        )
    }
}
