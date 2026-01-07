package cv.cbglib.services

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Service to load images and ONNX models from assets folder. Must be initialized in class that subclasses
 * [Application]. In this case automatically done by subclassing [cv.cbglib.CustomApplication], for use in
 * activities, fragments, etc... use:
 *
 * ```
 *     private val assetService =
 *         (context.applicationContext as SUBCLASSED_APP).assetService
 * ```
 *
 * Where SUBCLASSED_APP is a subclass of an [Application], or more precise [cv.cbglib.CustomApplication]. For more
 * detail see also [cv.cbglib.CustomApplication]
 */
class AssetService(private val app: Application) {
    companion object {
        // const val MODEL_PATH = "models/yolo_v8_aug.onnx"
        const val MODEL_PATH = "models/b8_w1_ep40_nosynth.onnx"
    }

    val modelByteArray: ByteArray by lazy {
        app.assets.open(MODEL_PATH).readBytes()
    }

    /**
     * Returns a [Bitmap] of image asset, image path does not have to be full, a recursive search will be applied until
     * a file with [filename] will be found (extension must be matching).
     */
    fun getImageBitmap(filename: String, rootDir: String = ""): Bitmap? {
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
                val result = getImageBitmap(filename, fullPath)
                if (result != null) {
                    return result
                }
            }
        }

        return null
    }
}