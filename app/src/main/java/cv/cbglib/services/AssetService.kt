package cv.cbglib.services

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 *
 */
class AssetService(private val app: Application) {
    companion object {
        const val MODEL_PATH = "models/yolo_v8_aug.onnx"
    }

    val modelByteArray: ByteArray by lazy {
        app.assets.open(MODEL_PATH).readBytes()
    }

    fun getImage(filename: String, rootDir: String = ""): Bitmap? {
        val files = app.assets.list(rootDir) ?: return null

        for (file in files) {
            val fullPath = if (rootDir.isEmpty()) file else "$rootDir/$file"
            if (file == filename) {
                val stream = app.assets.open(fullPath)
                val bitmap = BitmapFactory.decodeStream(stream)
                stream.close()
                return bitmap

            }

            val subFiles = app.assets.list(fullPath)
            if (!subFiles.isNullOrEmpty()) {
                val result = getImage(filename, fullPath)
                if (result != null) {
                    return result
                }
            }
        }

        return null
    }
}