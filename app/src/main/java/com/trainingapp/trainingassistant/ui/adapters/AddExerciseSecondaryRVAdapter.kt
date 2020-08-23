package com.trainingapp.trainingassistant.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.objects.MuscleJoint

/**
 * Adapter to display exercise secondary movers that the user can select. Multiple movers can be selected so an Array of Boolean values is kept to
 * keep track of which items are selected
 * @param muscleJoints List of all the possible muscles or joints the user can choose from
 * @param clickListener Function that handles the onClick event of an item within the AddEditExerciseActivity
 */
class AddExerciseSecondaryRVAdapter(
    val muscleJoints: List<MuscleJoint>,
    private val clickListener: (MuscleJoint, Boolean) -> Unit
): RecyclerView.Adapter<AddExerciseSecondaryRVAdapter.AddExerciseSecondaryViewHolder>() {

    // Running journal of which items are selected
    val isSelected = Array(muscleJoints.size) {false}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddExerciseSecondaryViewHolder {
        return AddExerciseSecondaryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.mover_row, parent, false))
    }

    override fun getItemCount(): Int = muscleJoints.size

    override fun onBindViewHolder(vh: AddExerciseSecondaryViewHolder, position: Int) {
        vh.onBindItems(muscleJoints[position], position)
    }

    inner class AddExerciseSecondaryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener{

        fun onBindItems(muscleJoint: MuscleJoint, position: Int){
            itemView.findViewById<TextView>(R.id.txtExerciseMoverName).text = muscleJoint.name
            // Used to internally handle the onClick event and use passed function
            itemView.setOnClickListener(this)
            itemView.isSelected = isSelected[position]
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            isSelected[position] = !isSelected[position]
            itemView.isSelected = !itemView.isSelected
            // Sends selected/deselected item to activity to add/remove the secondary mover
            clickListener(muscleJoints[position], isSelected[position])
            // Notify adapter that item state has changed
            notifyItemChanged(position)
        }
    }
}