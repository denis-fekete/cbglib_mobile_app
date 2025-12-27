package cv.demoapps.bangdemo.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import cv.cbglib.detection.CameraController
import cv.cbglib.detection.AssetService
import cv.demoapps.bangdemo.MainActivity
import cv.cbglib.commonUI.OverlayView
import cv.demoapps.bangdemo.R
import cv.demoapps.bangdemo.viewmodels.CameraViewModel
import kotlinx.coroutines.launch

class CameraFragment :
    BaseNavigationFragment(R.layout.fragment_camera) {
    private lateinit var cameraController: CameraController
    private lateinit var cameraxView: PreviewView
    private lateinit var overlayView: OverlayView

    private val viewModel: CameraViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        cameraxView = view.findViewById<PreviewView>(R.id.cameraxView)
        overlayView = view.findViewById<OverlayView>(R.id.overlayView)

        // fill view, crop excess
        cameraxView.scaleType = PreviewView.ScaleType.FILL_CENTER

        cameraController = CameraController(
            requireContext(),
            this as LifecycleOwner,
            cameraxView,
            overlayView
        )

        cameraController.init()

        lifecycleScope.launch {
            try {
                cameraController.startCamera()
            } catch (e: Exception) {
                Log.e("CV", "Camera initialization failed: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraController.destroy()
    }
}