package cv.demoapps.bangdemo.fragments

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import cv.cbglib.fragments.BaseFragment
import cv.cbglib.services.SettingsService
import cv.demoapps.bangdemo.MyApp
import cv.demoapps.bangdemo.R

class SettingsFragment : BaseFragment(R.layout.fragment_settings) {
    private lateinit var modelSpinner: Spinner
    private lateinit var languageSpinner: Spinner
    private var lastSelectedModel: String = ""
    private var lastSelectedLanguage: String = ""

    private val settingsService by lazy {
        (requireContext().applicationContext as MyApp).settingsService
    }

    private val assetService by lazy {
        (requireContext().applicationContext as MyApp).assetService
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupModelSpinner(view)
        setupLanguageSpinner(view)
    }

    private fun setupLanguageSpinner(view: View) {
        languageSpinner = view.findViewById<Spinner>(R.id.languageSpinner)

        val languageAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            SettingsService.languageOptions
        )

        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = languageAdapter

        val languageIndex = SettingsService.languageOptions.indexOf(settingsService.language)
        if (languageIndex != -1) {
            languageSpinner.setSelection(languageIndex, false)
        } else {
            languageSpinner.setSelection(0)
        }

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val current = parent?.getItemAtPosition(position).toString()
                if (current == lastSelectedModel) return

                lastSelectedLanguage = current
                settingsService.language = current
                settingsService.save()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupModelSpinner(view: View) {
        modelSpinner = view.findViewById<Spinner>(R.id.modelSpinner)

        val modelAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            assetService.availableModels
        )
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modelSpinner.adapter = modelAdapter

        val modelIndex = assetService.availableModels.indexOf(settingsService.selectedModel)
        if (modelIndex != -1) {
            modelSpinner.setSelection(modelIndex, false)
        } else {
            modelSpinner.setSelection(0)
        }

        modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val current = parent?.getItemAtPosition(position).toString()

                if (current == lastSelectedModel) return

                lastSelectedModel = current
                settingsService.selectedModel = current
                settingsService.save()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}