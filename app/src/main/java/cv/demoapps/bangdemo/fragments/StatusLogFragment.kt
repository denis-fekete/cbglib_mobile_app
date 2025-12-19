package cv.demoapps.bangdemo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cv.demoapps.bangdemo.R

/**
 * A simple [Fragment] subclass.
 */
class StatusLogFragment : BaseNavigationFragment(R.layout.fragment_status_log) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        return view
    }
}