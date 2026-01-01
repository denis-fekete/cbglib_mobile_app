package cv.cbglib;

import android.app.Application
import cv.cbglib.services.AssetService

abstract class CustomApplication : Application() {
    val assetService: AssetService by lazy {
        AssetService(this)
    }
}