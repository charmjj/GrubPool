package cs461.g2t10.grubpool

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

class ExpandableFiltersListViewAdapter internal constructor(private val context: Context, private val filterTypesList: List<String>, private val filtersList: HashMap<String, List<String>>):
    BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return filterTypesList.size
    }

    override fun getChildrenCount(groupPos: Int): Int {
        return this.filtersList[this.filterTypesList[groupPos]]!!.size
    }

    override fun getGroup(groupPos: Int): Any {
        return filterTypesList[groupPos]
    }

    override fun getChild(groupPos: Int, childPos: Int): Any {
        return this.filtersList[this.filterTypesList[groupPos]]!![childPos]
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
        val filterTitle = getChild(groupPos, childPos) as String

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.filters_list, null)
        }
        val filterTv = convertView!!.findViewById<TextView>(R.id.filtersTv)
        filterTv.setText(filterTitle)

        return convertView
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }
}