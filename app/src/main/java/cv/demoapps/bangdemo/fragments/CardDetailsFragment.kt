package cv.demoapps.bangdemo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import cv.demoapps.bangdemo.MyApp
import cv.demoapps.bangdemo.R

private const val ARG_DETECTION_ID = "detectionId"

/**
 * Fragment to used for Card details display,
 * Use the [CardDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CardDetailsFragment : Fragment() {
    private var detectionId: Int? = null
    lateinit var titleTextView: TextView
    lateinit var descriptionTextView: TextView
    lateinit var imageView: ImageView
    lateinit var symbolsLayout: LinearLayout

    private val assetService by lazy {
        (requireContext().applicationContext as MyApp).assetService
    }

    private val cardDetailsService by lazy {
        (requireContext().applicationContext as MyApp).cardDetailsService
    }

    private val symbolDetailsService by lazy {
        (requireContext().applicationContext as MyApp).symbolDetailsService
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            detectionId = it.getInt(ARG_DETECTION_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_card_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        titleTextView = view.findViewById<TextView>(R.id.titleText)
        descriptionTextView = view.findViewById<TextView>(R.id.descriptionText)
        imageView = view.findViewById<ImageView>(R.id.imageView)
        symbolsLayout = view.findViewById<LinearLayout>(R.id.symbolsLayout)

        symbolsLayout.removeAllViews()

        if (detectionId != null) {
            titleTextView.text = cardDetailsService.items[detectionId]?.name
            descriptionTextView.text = cardDetailsService.items[detectionId]?.description

            val bitmap =
                assetService.getImageBitmap("${cardDetailsService.items[detectionId]?.imagePath}", "card_scans")
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            }

            if (cardDetailsService.items[detectionId]?.symbols != null) {
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                params.setMargins(0, 0, 40, 0)

                for (symbol in cardDetailsService.items[detectionId]?.symbols!!) {
                    val imgPath = symbolDetailsService.items[symbol]?.imagePath ?: continue

                    val bitmap = assetService.getImageBitmap(imgPath, "")
                    val imgView = ImageView(context)
                    imgView.setImageBitmap(bitmap)
                    imgView.setOnClickListener {
                        Toast.makeText(
                            context,
                            "Clicked on ${symbolDetailsService.items[symbol]?.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    imgView.layoutParams = params

                    symbolsLayout.addView(imgView)

                }
            }

        }
    }

    companion object {
        /**
         * Factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param detectionId Detection ID that will be displayed in Fragment
         * @return A new instance of fragment CardDetailsFragment.
         */
        @JvmStatic
        fun newInstance(id: String) =
            CardDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DETECTION_ID, id)
                }
            }
    }
}