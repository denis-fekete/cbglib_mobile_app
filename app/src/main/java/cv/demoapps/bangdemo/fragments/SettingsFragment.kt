package cv.demoapps.bangdemo.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cv.demoapps.bangdemo.R
import cv.demoapps.bangdemo.viewmodels.SettingsViewModel

class SettingsFragment : BaseNavigationFragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModels()

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