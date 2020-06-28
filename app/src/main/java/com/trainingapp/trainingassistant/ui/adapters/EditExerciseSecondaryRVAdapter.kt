package com.trainingapp.trainingassistant.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.objects.MuscleJoint

/**
 * Adapter to display the secondary movers that an exercise already contains and can be edited
 * @param muscleJoints List of all the possible muscles or joints the user can choose from
 * @param secondaryMovers Movers contained within the Exercise that need to be set as selected upon init
 * @param clickListener Function that handles the onClick event of an item within the AddEditExerciseActivity
 */
class EditExerciseSecondaryRVAdapter(
    val muscleJoints: MutableList<MuscleJoint>,
    private val secondaryMovers: MutableList<MuscleJoint>,
    private val clickListener: (MuscleJoint, Boolean) -> Unit
): RecyclerView.Adapter<EditExerciseSecondaryRVAdapter.AddExerciseSecondaryViewHolder>() {

    val isSelected = Array(muscleJoints.size) {false}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddExerciseSecondaryViewHolder {
        if (secondaryMovers.isNotEmpty())//check to make sure the list has items
            muscleJoints.forEach { isSelected[muscleJoints.indexOf(it)] = secondaryMovers.contains(it) }//for each possible MuscleJoint, check to see if that item should be selected
        return AddExerciseSecondaryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.mover_row, parent, false))
    }

    override fun getItemCount(): Int = muscleJoints.size

    override fun onBindViewHolder(vh: AddExerciseSecondaryViewHolder, position: Int) {
        vh.onBindItems(muscleJoints[position], position)
    }

    inner class AddExerciseSecondaryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener{

        fun onBindItems(muscleJoint: MuscleJoint, position: Int){
            itemView.findViewById<TextView>(R.id.txtExerciseMoverName).text = muscleJoint.name
            itemView.setOnClickListener(this)//used to internally handle the onClick event and use passed function
            itemView.isSelected = isSelected[position]
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            isSelected[position] = !isSelected[position]
            itemView.isSelected = !itemView.isSelected
            clickListener(muscleJoints[position], isSelected[position])//sends selected/deselected item to activity to add/remove the secondary mover
            notifyItemChanged(position)//notify adapter that item state has changed
        }
    }
}