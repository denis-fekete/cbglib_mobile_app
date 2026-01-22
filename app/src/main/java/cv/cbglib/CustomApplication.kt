package cv.cbglib;

import android.app.Application
import cv.cbglib.services.AssetService
import cv.cbglib.services.SettingsService

/**
 * Application that initializes [AssetService], needed for the [cv.cbglib.fragments.AbstractCameraFragment]. For use
 * subclass this class and set it in `AndroidManifest.xml`:
 *
 *  <application
 *             android:name = ".MyApp"
 *
 *  Where the name of derived class from this class is `MyApp` (can be whatever else, just must match the
 *  manifest). Continue adding code in `MyApp` class.
 *
 */
abstract class CustomApplication : Application() {
    val assetService: AssetService by lazy {
        AssetService(this)
    }

    val settingsService: SettingsService by lazy {
        SettingsService(this)
    }
}