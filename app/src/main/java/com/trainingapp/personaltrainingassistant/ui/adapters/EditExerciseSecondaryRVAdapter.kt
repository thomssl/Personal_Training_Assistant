package com.trainingapp.personaltrainingassistant.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trainingapp.personaltrainingassistant.R
import com.trainingapp.personaltrainingassistant.objects.MuscleJoint

class EditExerciseSecondaryRVAdapter(val muscleJoints: ArrayList<MuscleJoint>, private val secondaryMovers: ArrayList<MuscleJoint>, private val clickListener: (MuscleJoint, Boolean) -> Unit): RecyclerView.Adapter<EditExerciseSecondaryRVAdapter.AddExerciseSecondaryViewHolder>() {

    val isSelected = Array(muscleJoints.size) {false}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddExerciseSecondaryViewHolder {
        if (secondaryMovers.isNotEmpty())
            muscleJoints.forEach { isSelected[muscleJoints.indexOf(it)] = secondaryMovers.contains(it) }
        return AddExerciseSecondaryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.mover_row, parent, false))
    }

    override fun getItemCount(): Int = muscleJoints.size

    override fun onBindViewHolder(vh: AddExerciseSecondaryViewHolder, position: Int) {
        vh.onBindItems(muscleJoints[position], position)
    }

    inner class AddExerciseSecondaryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener{

        fun onBindItems(muscleJoint: MuscleJoint, position: Int){
            itemView.findViewById<TextView>(R.id.txtExerciseMoverName).text = muscleJoint.name
            itemView.setOnClickListener(this)
            itemView.isSelected = isSelected[position]
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            isSelected[position] = !isSelected[position]
            itemView.isSelected = !itemView.isSelected
            clickListener(muscleJoints[position], isSelected[position])
            notifyItemChanged(position)
        }
    }
}