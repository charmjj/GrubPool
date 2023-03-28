package cs461.g2t10.grubpool

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView

/**
 * A simple [Fragment] subclass.
 * Use the [FindGrubFilteringPanel.newInstance] factory method to
 * create an instance of this fragment.
 */
class FindGrubFilteringPanel : Fragment() {

    private lateinit var listViewAdapter: ExpandableFiltersListViewAdapter
    private var filterTypesList: List<String> = listOf("Cuisine", "Dietary Restrictions", "Time Listed")
    private var filtersList: HashMap<String, List<String>> = hashMapOf()

//    val defaultFilters: LinkedHashMap<String, String> =  linkedMapOf("Cuisine" to "All Cuisines", "Dietary Restrictions" to "No Restrictions", "Time Listed" to "All Time");
//    var selectedFilters: LinkedHashMap<String, String> =  linkedMapOf("Cuisine" to "All Cuisines", "Dietary Restrictions" to "No Restrictions", "Time Listed" to "All Time");

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        filtersList["Cuisine"] = listOf("Japanese", "Chinese", "Italian", "Greek", "Spanish", "Others","All Cuisines")
        filtersList["Dietary Restrictions"] = listOf("Halal", "Vegetarian", "No Beef", "No Restrictions")
        filtersList["Time Listed"] = listOf("Last Hour", "Last 3 Hours", "Last 5 Hours", "All Time")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val context = activity as Context
        val view = inflater.inflate(R.layout.fragment_find_grub_filtering_panel, container, false)
        listViewAdapter = ExpandableFiltersListViewAdapter(context, filterTypesList, filtersList)
        val filtersListView = view.findViewById<ExpandableListView>(R.id.filtersListView)
        filtersListView.setAdapter(listViewAdapter)
        return view
    }
}