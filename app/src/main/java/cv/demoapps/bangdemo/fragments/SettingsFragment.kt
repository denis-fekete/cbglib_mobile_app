package cv.demoapps.bangdemo.fragments

import androidx.fragment.app.viewModels
import cv.cbglib.fragments.BaseFragment
import cv.demoapps.bangdemo.R
import cv.demoapps.bangdemo.viewmodels.SettingsViewModel

class SettingsFragment : BaseFragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModels()

}