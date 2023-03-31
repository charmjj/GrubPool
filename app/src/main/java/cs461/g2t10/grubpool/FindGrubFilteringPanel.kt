package cs461.g2t10.grubpool

import android.content.Context
import android.os.Bundle
import android.util.Log
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
    private lateinit var filtersListView: ExpandableListView

    private var filterTypesList: List<String> = listOf("Cuisine", "Dietary Restrictions", "Time Listed")
    private var filtersList: HashMap<String, List<String>> = hashMapOf()
    private val defaultSelections: HashMap<String, List<String>> = hashMapOf(
        "Cuisine" to listOf("All Cuisines"),
        "Dietary Restrictions" to listOf("No Restrictions"),
        "Time Listed" to listOf("All Time"))
    private val defaultValues: HashMap<String, String> = hashMapOf(
        "Cuisine" to "All Cuisines",
        "Dietary Restrictions" to "No Restrictions"
    )
    private var currentSelections = HashMap<String, MutableList<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        filtersList["Cuisine"] = listOf("Japanese", "Chinese", "Italian", "Greek", "Spanish", "Others","All Cuisines")
        filtersList["Dietary Restrictions"] = listOf("Halal", "Vegetarian", "No Beef", "No Restrictions")
        filtersList["Time Listed"] = listOf("Last Hour", "Last 3 Hours", "Last 5 Hours", "All Time")

        defaultSelections.forEach { (key, value) ->
            currentSelections[key] = value.toMutableList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val context = activity as Context
        val view = inflater.inflate(R.layout.fragment_find_grub_filtering_panel, container, false)

        listViewAdapter = ExpandableFiltersListViewAdapter(context, filterTypesList, filtersList, currentSelections)
        filtersListView = view.findViewById<ExpandableListView>(R.id.filtersListView)
        filtersListView.setAdapter(listViewAdapter)

        filtersListView.setOnChildClickListener { parent, view, groupPosition, childPosition, id ->
            val adapter = parent.expandableListAdapter as ExpandableFiltersListViewAdapter // can use listViewAdapter instead?
            val filterTypeTitle = adapter.getGroup(groupPosition) as String
            val childData = adapter.getChild(groupPosition, childPosition) as Pair<String, Boolean> // Pair(childText, selected)
            val filterValue = childData.first

            val checkMarkView = view.findViewById<ImageView>(R.id.checkMark)

            // not multi-select, unselect is only done by selecting another option
            if (filterTypeTitle == "Time Listed" && !currentSelections[filterTypeTitle]?.contains(filterValue)!!) {
                currentSelections[filterTypeTitle]?.clear()
                currentSelections[filterTypeTitle]?.add(filterValue)
                listViewAdapter.updateSelections(currentSelections)
                listViewAdapter.notifyDataSetChanged()
            } else { // filter type falls under Cuisine or Dietary Restrictions
                // user is unselecting
                if (currentSelections[filterTypeTitle]?.contains(filterValue)!!) {
                    // current selection is not the only selection --> then can remove
                    if (currentSelections[filterTypeTitle]?.size!! > 1) {
                        currentSelections[filterTypeTitle]?.remove(filterValue)
                        checkMarkView.visibility = View.GONE
                    }
                } else { // User is SELECTING
                    if (filterValue == defaultValues[filterTypeTitle]) { // user selected default --> need to clear everything else
                        currentSelections[filterTypeTitle]?.clear()
                        currentSelections[filterTypeTitle]?.add(filterValue)
                    } else {
                        currentSelections[filterTypeTitle]?.remove(defaultValues[filterTypeTitle])
                        currentSelections[filterTypeTitle]?.add(filterValue)
                    }
                    listViewAdapter.updateSelections(currentSelections)
                    listViewAdapter.notifyDataSetChanged()
                }
            }
            true
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

    private fun applyFilters() {
        Log.d("APPLYINGGG:", currentSelections.toString())
        val filterPanelBehavior = (activity as FindGrubActivity).filterPanelBehavior
        filterPanelBehavior?.let {
                it.state = BottomSheetBehavior.STATE_HIDDEN
        }
        (activity as FindGrubActivity).filterDeals(currentSelections).start()
    }

    private fun resetCurrentSelections() {
        currentSelections.clear()
        defaultSelections.forEach { (key, value) ->
            currentSelections[key] = value.toMutableList()
        }

        listViewAdapter.updateSelections(currentSelections)
        listViewAdapter.notifyDataSetChanged()
    }
}