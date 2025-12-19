package cv.demoapps.bangdemo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.navigation.findNavController
import cv.demoapps.bangdemo.R


/**
 * A simple [Fragment] subclass.
 * Use the [NavigationPanelFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NavigationPanelFragment() : Fragment() {
    private lateinit var btnCamera: ImageButton
    private lateinit var btnSettings: ImageButton
    private lateinit var btnStatusLog: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_navigation_panel,
            container,
            false
        )

        btnCamera = view.findViewById<ImageButton>(R.id.btnNavCamera)
        btnSettings = view.findViewById<ImageButton>(R.id.btnNavSettings)
        btnStatusLog = view.findViewById<ImageButton>(R.id.btnNavStatusLog)


        btnCamera.setOnClickListener {
            view.findNavController().navigate(R.id.cameraFragment)
        }

        btnSettings.setOnClickListener {
            view.findNavController().navigate(R.id.settingsFragment)
        }

        btnStatusLog.setOnClickListener {
            view.findNavController().navigate(R.id.statusLogFragment)
        }

        return view
    }
}