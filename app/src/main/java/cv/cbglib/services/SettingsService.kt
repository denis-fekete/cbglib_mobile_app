package cv.cbglib.services

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class SettingsService(
    private val app: Application,
) {
    lateinit var language: String // TODO: implement behavior
    lateinit var detectionBoxTextColor: String // TODO: implement behavior
    lateinit var detectionBoxTextBackground: String // TODO: implement behavior
    lateinit var detectionBoxColor: String // TODO: implement behavior
    var textSize: Int = 0 // TODO: implement behavior
    lateinit var selectedModel: String
    var showPerformance: Boolean = false
    var verbosePerformance: Boolean = false
    var framesToSkip: Int = 5

    var sPref: SharedPreferences = app.getSharedPreferences(
        "settings",
        Context.MODE_PRIVATE
    )

    init {
        load()
    }

    /**
     * Loads settings from `SharedPreferences`.
     */
    fun load() {
        selectedModel = sPref.getString("selectedModel", null).toString()
        language = sPref.getString("language", "").toString()

        detectionBoxTextBackground = sPref.getString("detectionBoxTextBackground", "").toString()
        detectionBoxTextColor = sPref.getString("detectionBoxTextColor", "").toString()
        detectionBoxColor = sPref.getString("detectionBoxColor", "").toString()

        textSize = sPref.getInt("detectionBoxTextSize", 0)

        showPerformance = sPref.getBoolean("showPerformance", false)
        verbosePerformance = sPref.getBoolean("verbosePerformance", true)

        framesToSkip = sPref.getInt("framesToSkip", 0)
    }

    /**
     * Saves settings to the `SharedPreferences`.
     */
    fun save() {
        val editor = sPref.edit()
        editor.apply {
            putString("selectedModel", selectedModel)
            putString("language", language)

            putString("detectionBoxTextBackground", detectionBoxTextBackground)
            putString("detectionBoxTextColor", detectionBoxTextColor)
            putString("detectionBoxColor", detectionBoxColor)

            putInt("detectionBoxTextSize", textSize)

            putBoolean("showPerformance", showPerformance)
            putBoolean("verbosePerformance", verbosePerformance)

            putInt("framesToSkip", framesToSkip)

        }.apply()
    }

    companion object {
        val languageOptions = listOf("Čestina", "English", "Slovenčina")
    }
}