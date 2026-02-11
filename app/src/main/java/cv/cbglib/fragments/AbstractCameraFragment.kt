package cv.cbglib.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import cv.cbglib.detection.OverlayView
import cv.cbglib.detection.CameraController
import cv.demoapps.bangdemo.R
import kotlinx.coroutines.launch

/**
 * Abstract class containing code for fragments that contain camera preview with detections.
 * Initializes [CameraController] and sets up preview of camera and detection mechanism.
 * This fragment must be linked to a layout that contains [PreviewView] named with id
 * [cameraxView] and [OverlayView] with id name [overlayView]. These IDs must match, or [initViews] must be
 * overridden and IDs corrected (for this look implementation of [initViews] in base class).
 *
 * @param layoutRes is and android ID of layout the derived class if bound to (example: `R.layout.fragment_camera`)
 */
abstract class AbstractCameraFragment(layoutRes: Int) : BaseFragment(layoutRes) {
    private lateinit var cameraController: CameraController
    protected lateinit var cameraxView: PreviewView
    protected lateinit var overlayView: OverlayView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // find views by ID and initialize them
        initViews(view)

        // scale view to use as much screen space, keep ration and crop excess
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

    /**
     * Initializes [cameraxView] and [overlayView] which must be initialized for camera preview and detections preview.
     *
     * @param view is a View for finding Views by their IDs in layout
     */
    protected fun initViews(view: View) {
        cameraxView = view.findViewById<PreviewView>(R.id.cameraxView)
        overlayView = view.findViewById<OverlayView>(R.id.overlayView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraController.destroy()
    }
}