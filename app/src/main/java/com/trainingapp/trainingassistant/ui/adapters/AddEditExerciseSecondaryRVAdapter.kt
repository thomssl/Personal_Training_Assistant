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
class AddEditExerciseSecondaryRVAdapter(
    val muscleJoints: List<MuscleJoint>,
    private val secondaryMovers: List<MuscleJoint>,
    private val clickListener: (MuscleJoint, Boolean) -> Unit
): RecyclerView.Adapter<AddEditExerciseSecondaryRVAdapter.AddExerciseSecondaryViewHolder>() {

    val isSelected = muscleJoints.map { secondaryMovers.contains(it) }.toTypedArray()

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