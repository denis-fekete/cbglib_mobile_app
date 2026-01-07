package cv.demoapps.bangdemo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity() {
    private lateinit var btnCamera: ImageButton
    private lateinit var btnSettings: ImageButton
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigation()


        if (OpenCVLoader.initLocal()) {
            Log.d("OpenCV", "OpenCV loaded successfully")
        } else {
            Log.e("OpenCV", "Failed to load OpenCV")
        }


        checkCameraPermission(this, this)
        checkStoragePermission(this, this)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostContainer) as NavHostFragment

        navController = navHostFragment.navController

        btnCamera = findViewById<ImageButton>(R.id.btnNavCamera)
        btnSettings = findViewById<ImageButton>(R.id.btnNavSettings)

        btnCamera.setOnClickListener {
            navController.navigate(R.id.cameraFragment)
        }

        btnSettings.setOnClickListener {
            navController.navigate(R.id.settingsFragment)
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
        private const val STORAGE_PERMISSION_REQUEST_CODE = 1

        fun checkCameraPermission(context: Context, activity: MainActivity) {
            // Request camera access
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // If permission was not granted, request it
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        }

        fun checkStoragePermission(context: Context, activity: MainActivity) {
            // Request camera access
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // If permission was not granted, request it
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
}