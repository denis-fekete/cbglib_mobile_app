package cv.cbglib.services

import android.app.Application
import android.util.Log
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Template class used for loading an JSON files from `assets` as a [Map] of integer keys and data class objects.
 * Use:
 * Create a `@serializable data class` class that will contain data in JSON file, it is expected that the JSON file is
 * a list of these objects. This object must contain some ID that is Int, this will be used for accessing.
 *
 * Declaration in class that subclassed [cv.cbglib.CustomApplication]:
 * ```
 * val DATA_CLASS_SERVICE: JsonAssetService<DATA_CLASS_NAME> by lazy {
 *         JsonAssetService<SymbolDetail>(
 *             this,
 *             fileName = "JSON_FILE_PATH_IN_ASSETS.json",
 *             serializer = DATA_CLASS_NAME.serializer(),
 *             keySelector = { it.DATA_CLASS_NAME.PROPERTY_OF_TYPE_INT }
 *         )
 *     }
 * ```
 *
 * Use in any other activity, fragments, class, etc... (SUBCLASSED_APP is a class that subclassed
 * [cv.cbglib.CustomApplication] and was added to `AndroidManifest.xml`, for more
 * detail see [cv.cbglib.CustomApplication]):
 * ```
 *     private val DATA_CLASS_NAME =
 *         (context.applicationContext as SUBCLASSED_APP).DATA_CLASS_SERVICE
 * ```
 */
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