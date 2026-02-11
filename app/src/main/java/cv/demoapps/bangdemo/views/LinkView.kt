package cv.demoapps.bangdemo.views

import android.content.Context
import android.graphics.Bitmap
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cv.demoapps.bangdemo.R


class LinkView(private val context: Context?, private val text: String?, private val bitmap: Bitmap?) :
    LinearLayout(context) {

    init {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        params.setMargins(0, 0, 40, 0)
        this.layoutParams = params
        this.orientation = HORIZONTAL
        this.gravity = Gravity.CENTER

        LayoutInflater.from(context).inflate(R.layout.view_link, this, true)

        val imageView = findViewById<ImageView>(R.id.imageView)
        val textView = findViewById<TextView>(R.id.textView)
        val textContainer = findViewById<FrameLayout>(R.id.textContainer)

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
            textContainer.visibility = GONE
        } else {
            imageView.visibility = GONE

            textView.text = text ?: "Default"
        }
    }
}