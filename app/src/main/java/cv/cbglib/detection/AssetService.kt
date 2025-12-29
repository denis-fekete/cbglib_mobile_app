package cv.cbglib.detection

import android.app.Application
import android.util.Log

/**
 *
 */
class AssetService(private val app: Application) {
    companion object {
        const val MODEL_PATH = "models/yolo_v8_aug.onnx"
        const val LABELS_PATH = "models/labels.txt"
    }

    val modelByteArray: ByteArray by lazy {
        app.assets.open(MODEL_PATH).readBytes()
    }
    
    val labels: Map<Int, String>? by lazy {
        try {
            mutableMapOf<Int, String>().apply {
                app.assets.open(LABELS_PATH)
                    .bufferedReader()
                    .useLines { lines ->
                        for (line in lines) {
                            val segmented = line.split(":", limit = 2)
                            if (segmented.size == 2) {
                                val key = segmented[0].trim().toIntOrNull()
                                if (key != null) {
                                    this[key] = segmented[1].trim()
                                }
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e("AssetManager", "Failed to read asset", e)
            null
        }
    }
}