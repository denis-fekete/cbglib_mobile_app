package cv.demoapps.bangdemo.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import cv.cbglib.fragments.AbstractCameraFragment
import cv.demoapps.bangdemo.R

class CameraFragment : AbstractCameraFragment(R.layout.fragment_camera) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        overlayView.onDetectionClicked = { detection ->
            val action =
                CameraFragmentDirections.actionCameraFragmentToCardDetailsFragment(detection.classIndex)

            findNavController().navigate(action)
        }
    }
}