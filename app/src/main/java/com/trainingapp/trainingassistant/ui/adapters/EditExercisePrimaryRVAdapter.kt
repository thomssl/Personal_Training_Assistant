package com.trainingapp.trainingassistant.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.objects.MuscleJoint

/**
 * Adapter to display the primary mover that an exercise already contains and can be edited
 * @param muscleJoints List of all the possible muscles or joints the user can choose from
 * @param indexSelected index of the primary mover contained within the Exercise that need to be set as selected upon init
 * @param clickListener Function that handles the onClick event of an item within the AddEditExerciseActivity
 */
class EditExercisePrimaryRVAdapter(val muscleJoints: ArrayList<MuscleJoint>, indexSelected: Int, private val clickListener: (MuscleJoint) -> Unit): RecyclerView.Adapter<EditExercisePrimaryRVAdapter.AddExercisePrimaryViewHolder>() {

    var selectedPosition: Int = if (indexSelected >= 0) indexSelected else -1//used to tell which item is selected. init as -1 if passed index is invalid, else set as invalid index

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddExercisePrimaryViewHolder {
        return AddExercisePrimaryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.mover_row, parent, false))
    }

    override fun getItemCount(): Int = muscleJoints.size

    override fun onBindViewHolder(vh: AddExercisePrimaryViewHolder, position: Int) {
        vh.onBindItems(muscleJoints[position], position)
    }

    inner class AddExercisePrimaryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener{

        fun onBindItems(muscleJoint: MuscleJoint, position: Int){
            itemView.findViewById<TextView>(R.id.txtExerciseMoverName).text = muscleJoint.name
            itemView.setOnClickListener(this)//used to internally handle the onClick event and use passed function
            itemView.isSelected = selectedPosition == position
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            selectedPosition = position
            clickListener(muscleJoints[position])
            notifyDataSetChanged()//needed to update all items to new selected item
        }
    }
}