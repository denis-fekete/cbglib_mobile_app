package cv.demoapps.bangdemo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Abstract Fragment class used for easier implementation of navigation
 * buttons from [NavigationPanelFragment]. Derived [Fragment] class must
 * contain and [NavigationPanelFragment] in its XML file with id
 * `navigationPanel`. [layoutId] is an ID from `R.layout.` and must correspond
 * to the xml file that this fragment is belongs to.
 */
abstract class BaseNavigationFragment(
    private val layoutId: Int
) : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(layoutId, container, false)
    }
}