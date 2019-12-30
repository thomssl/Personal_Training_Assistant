package com.trainingapp.personaltrainingassistant.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.trainingapp.personaltrainingassistant.R
import com.trainingapp.personaltrainingassistant.objects.Exercise
import kotlinx.android.synthetic.main.simple_autocomplete_item.view.*
import java.util.*
import kotlin.collections.ArrayList

class SearchForExerciseSession(context: Context, private val resource: Int, val exercises: ArrayList<Exercise>) : ArrayAdapter<String>(context, resource) {

    var resultData = ArrayList<Exercise>()

    override fun getCount(): Int {
        return resultData.size
    }

    override fun getItem(position: Int): String {
        return resultData[position].name
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return if (convertView == null){
            val view = LayoutInflater.from(context).inflate(resource, parent, false)
            val txtName = view.findViewById<TextView>(R.id.txtSimpleAutocompleteItem)
            txtName.text = getItem(position)
            view
        } else {
            val txtName = convertView.findViewById<TextView>(R.id.txtSimpleAutocompleteItem)
            txtName.text = getItem(position)
            convertView
        }
    }

    override fun getFilter(): Filter {
        return ListFilter()
    }

    inner class ListFilter : Filter(){

        override fun performFiltering(input: CharSequence): FilterResults {
            val filterResults = FilterResults()
            resultData.clear()
            if (input.isBlank()){
                resultData = ArrayList(exercises.toList())
                filterResults.values = resultData
                filterResults.count = resultData.size
            } else {
                exercises.forEach {
                    if (it.name.toLowerCase(Locale.ROOT).contains(input.toString().toLowerCase(Locale.ROOT)) || it.primaryMover.name.toLowerCase(Locale.ROOT).contains(input.toString().toLowerCase(Locale.ROOT)))
                        resultData.add(it)
                }
                filterResults.values = resultData
                filterResults.count = resultData.size
            }
            return filterResults
        }

        override fun publishResults(p0: CharSequence?, results: FilterResults) {
            if (results.count > 0){
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }
}