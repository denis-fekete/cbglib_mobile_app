package cv.cbglib.services

import android.app.Application
import android.util.Log
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class JsonAssetService<T : Any>(
    private val app: Application,
    private val fileName: String,
    private val serializer: KSerializer<T>,
    private val keySelector: (T) -> Int
) {
    val items: Map<Int, T> by lazy {
        try {
            val jsonText = app.assets.open(fileName)
                .bufferedReader()
                .use { it.readText() }

            val list = Json.decodeFromString(ListSerializer(serializer), jsonText)
            list.associateBy(keySelector)

        } catch (e: Exception) {
            Log.e("AssetManager", "Failed to read $fileName. $e")
            emptyMap()
        }
    }
}