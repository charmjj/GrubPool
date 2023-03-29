package cs461.g2t10.grubpool

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ExpandableListView
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetBehavior

class FindGrubFilteringPanel : Fragment() {

    private lateinit var listViewAdapter: ExpandableFiltersListViewAdapter
    private var filterTypesList: List<String> = listOf("Cuisine", "Dietary Restrictions", "Time Listed")
    private var filtersList: HashMap<String, List<String>> = hashMapOf()
    private val defaultSelections: HashMap<String, List<String>> = hashMapOf(
        "Cuisine" to mutableListOf("All Cuisines"),
        "Dietary Restrictions" to mutableListOf("No Restrictions"),
        "Time Listed" to mutableListOf("All Time"))
    private var currentSelections = HashMap<String, List<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        filtersList["Cuisine"] = listOf("Japanese", "Chinese", "Italian", "Greek", "Spanish", "Others","All Cuisines") // 'All Cuisines' or multiselect
        filtersList["Dietary Restrictions"] = listOf("Halal", "Vegetarian", "No Beef", "No Restrictions") // 'No Restrictions' or multiselect
        filtersList["Time Listed"] = listOf("Last Hour", "Last 3 Hours", "Last 5 Hours", "All Time") // select 1 only

        currentSelections.putAll(defaultSelections)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val context = activity as Context
        val view = inflater.inflate(R.layout.fragment_find_grub_filtering_panel, container, false)

        listViewAdapter = ExpandableFiltersListViewAdapter(context, filterTypesList, filtersList, currentSelections) // or defaultSelections
        val filtersListView = view.findViewById<ExpandableListView>(R.id.filtersListView)
        filtersListView.setAdapter(listViewAdapter)

        filtersListView.setOnChildClickListener { parent, view, groupPosition, childPosition, id ->
            val childView = view.findViewById<ImageView>(R.id.checkMark)
            onFilterClick(childView, groupPosition, childPosition)
        }

        // set button listeners
        val resetBtn = view.findViewById<Button>(R.id.resetBtn)
        resetBtn.setOnClickListener {
            resetCurrentSelections()
        }
        val applyFiltersBtn = view.findViewById<Button>(R.id.applyFiltersBtn)
        applyFiltersBtn.setOnClickListener {
            applyFilters()
        }
        return view
    }

    private fun onFilterClick(childView: ImageView, groupPosition: Int, childPosition: Int): Boolean {
        //  TODO: update data
        // update UI
        if (childView.visibility == View.VISIBLE) {
            childView.visibility = View.GONE
        } else {
            childView.visibility = View.VISIBLE
        }
        return true
    }

    private fun applyFilters() {
        val filterPanelBehavior = (activity as FindGrubActivity).filterPanelBehavior
        filterPanelBehavior?.let {
                it.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun resetCurrentSelections() {
        currentSelections.clear()
        currentSelections.putAll(defaultSelections)
        // TODO: update expandablelistview
    }
}