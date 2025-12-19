package cv.cbglib.detection

import android.app.Application
import android.util.Log

/**
 *
 */
class AssetService(private val app: Application) {
    companion object {
        const val MODEL_PATH = "yolo_v8_aug.onnx"
        const val LABELS_PATH = "labels.txt"
    }

    val modelByteArray: ByteArray by lazy {
        app.assets.open(MODEL_PATH).readBytes()
    }

    val labels: Array<String>? by lazy {
        try {
            val str = app.assets.open(LABELS_PATH).bufferedReader().use { it.readText() }
            val strArr = str.split("\n").toTypedArray()

            strArr
        } catch (e: Exception) {
            Log.e("AssetManager", "Failed to read asset")

            null
        }
    }
}