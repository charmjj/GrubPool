package cs461.g2t10.grubpool

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.TextView

class ExpandableFiltersListViewAdapter internal constructor(private val context: Context,
                                                            private val filterTypesList: List<String>,
                                                            private val filtersList: HashMap<String, List<String>>,
                                                            private val currentSelections: HashMap<String, List<String>>): BaseExpandableListAdapter() {

    private var expandableListView: ExpandableListView? = null
    private var lastExpandedPosition = -1

    override fun getGroupCount(): Int {
        return filterTypesList.size
    }

    override fun getChildrenCount(groupPos: Int): Int {
        return this.filtersList[this.filterTypesList[groupPos]]!!.size
    }

    override fun getGroup(groupPos: Int): Any {
        return filterTypesList[groupPos]
    }

    override fun getChild(groupPos: Int, childPos: Int): Any { // gets DATA OBJ for specific child item
        val parentText = this.filterTypesList[groupPos]
        val childText = this.filtersList[parentText]!![childPos]
        var selected = false
        if (currentSelections[parentText]!!.contains(childText)) {
            selected = true
        }
        return Pair(childText, selected)
        //return this.filtersList[this.filterTypesList[groupPos]]!![childPos]
    }

    override fun getGroupId(groupPos: Int): Long {
        return groupPos.toLong()
    }

    override fun getChildId(groupPos: Int, childPos: Int): Long {
        return childPos.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPos: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        if (expandableListView == null) {
            expandableListView = parent as ExpandableListView
        }

        var convertView = convertView
        val filterTypeTitle = getGroup(groupPos) as String

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.filter_types_list, null)
        }
        val filterTypeTv = convertView!!.findViewById<TextView>(R.id.filterTypeTv)
        filterTypeTv.setText(filterTypeTitle)

        return convertView
    }

    override fun getChildView(groupPos: Int, childPos: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        val childData = getChild(groupPos, childPos) as Pair<String, Boolean> // Pair(childText, selected)
        val filterTitle = childData.first
        val isFilterSelected = childData.second

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.filters_list, null)
        }
        val filterTv = convertView!!.findViewById<TextView>(R.id.filtersTv)
        filterTv.setText(filterTitle)
        val filterCheckMark = convertView!!.findViewById<ImageView>(R.id.checkMark)
        if (isFilterSelected) {
            filterCheckMark.visibility = View.VISIBLE
        } else {
            filterCheckMark.visibility = View.GONE
        }

        return convertView
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }

    override fun onGroupExpanded(groupPosition: Int) {
        if (lastExpandedPosition != -1 && groupPosition != lastExpandedPosition) {
            expandableListView?.collapseGroup(lastExpandedPosition)
        }
        lastExpandedPosition = groupPosition
    }

}