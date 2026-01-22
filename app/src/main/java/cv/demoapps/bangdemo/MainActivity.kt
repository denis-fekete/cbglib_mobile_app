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
import cv.cbglib.services.PermissionService
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

        PermissionService.checkCameraPermission(this, this)
        PermissionService.checkStoragePermission(this, this)
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
}