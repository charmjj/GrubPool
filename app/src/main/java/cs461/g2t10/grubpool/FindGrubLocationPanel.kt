package cs461.g2t10.grubpool

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class FindGrubLocationPanel : Fragment() {
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
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        val button = view.findViewById(R.id.searchBtn) as Button
        button.setOnClickListener {
            searchLocation()
        }
        return view
    }

    private fun searchLocation() {
        var location = searchField.text.toString().trim()
        if (location == null || location == "") {
            Toast.makeText(activity, "Provide location!", Toast.LENGTH_SHORT).show()
        } else {
            searchField.text.clear()
            searchField.hideKeyboard()
            (activity as FindGrubActivity).searchLocation(location)
        }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}