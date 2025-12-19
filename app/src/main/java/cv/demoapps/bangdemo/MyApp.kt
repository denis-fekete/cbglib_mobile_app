package cv.demoapps.bangdemo


import android.app.Application
import cv.cbglib.detection.AssetService

class MyApp : Application() {

    val assetService: AssetService by lazy {
        AssetService(this)
    }
}