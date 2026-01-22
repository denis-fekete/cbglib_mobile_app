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
    var sPref: SharedPreferences

    init {
        sPref = app.getSharedPreferences(
            "settings",
            Context.MODE_PRIVATE
        )

        load()
    }

    fun load() {
        selectedModel = sPref.getString("selectedModel", null).toString()
        language = sPref.getString("language", "").toString()

        detectionBoxTextBackground = sPref.getString("detectionBoxTextBackground", "").toString()
        detectionBoxTextColor = sPref.getString("detectionBoxTextColor", "").toString()
        detectionBoxColor = sPref.getString("detectionBoxColor", "").toString()

        textSize = sPref.getInt("detectionBoxTextSize", 0)
    }

    fun save() {
        val editor = sPref.edit()
        editor.apply {
            putString("selectedModel", selectedModel)
            putString("language", language)

            putString("detectionBoxTextBackground", detectionBoxTextBackground)
            putString("detectionBoxTextColor", detectionBoxTextColor)
            putString("detectionBoxColor", detectionBoxColor)

            putInt("detectionBoxTextSize", textSize)
        }.apply()
    }

    companion object {
        val languageOptions = listOf("Čestina", "English", "Slovenčina")
    }
}