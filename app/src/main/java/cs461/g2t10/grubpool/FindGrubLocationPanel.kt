package cs461.g2t10.grubpool

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior


class FindGrubLocationPanel : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var searchField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_find_grub_location_panel, container, false)
        searchField = view.findViewById(R.id.et_search)
        val locationPanelBehavior = (activity as FindGrubActivity).locationPanelBehavior
        searchField.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                locationPanelBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        val button = view.findViewById(R.id.searchBtn) as Button
        button.setOnClickListener {
            searchLocation()
        }
        return view
    }

    fun searchLocation() {
        var location = searchField.text.toString().trim()
        if (location == null || location == "") {
            Toast.makeText(activity, "Provide location!", Toast.LENGTH_SHORT).show()
        } else {
            searchField.text.clear()
            searchField.hideKeyboard()
            (activity as FindGrubActivity).searchLocation(location)
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}