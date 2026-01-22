package cv.demoapps.bangdemo.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import cv.cbglib.fragments.AbstractCameraFragment
import cv.demoapps.bangdemo.R

/**
 * [CameraFragment] is class derived from [AbstractCameraFragment]. Basic functionality can be achieved by simply
 * inheriting from class, giving current layout "ID". On detection click must be activated here! The
 * [CameraFragmentDirections] is needed for navigation unless other navigation system is used.
 */
class CameraFragment : AbstractCameraFragment(R.layout.fragment_camera) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // attaching onDetectionClicked event
        overlayView.onDetectionClicked = { detection ->
            val action =
                CameraFragmentDirections.actionCameraFragmentToCardDetailsFragment(detection.classIndex)

            findNavController().navigate(action)
        }
    }
}