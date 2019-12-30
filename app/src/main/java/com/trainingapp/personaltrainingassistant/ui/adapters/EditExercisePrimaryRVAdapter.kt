package com.trainingapp.personaltrainingassistant.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trainingapp.personaltrainingassistant.R
import com.trainingapp.personaltrainingassistant.objects.MuscleJoint

class EditExercisePrimaryRVAdapter(val muscleJoints: ArrayList<MuscleJoint>,private val indexSelected: Int, private val clickListener: (MuscleJoint) -> Unit): RecyclerView.Adapter<EditExercisePrimaryRVAdapter.AddExercisePrimaryViewHolder>() {

    var isSelected = Array(muscleJoints.size) {false}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddExercisePrimaryViewHolder {
        if (indexSelected >= 0)
            isSelected[indexSelected] = true
        return AddExercisePrimaryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.mover_row, parent, false))
    }

    override fun getItemCount(): Int = muscleJoints.size

    override fun onBindViewHolder(vh: AddExercisePrimaryViewHolder, position: Int) {
        vh.onBindItems(muscleJoints[position], position)
    }

    inner class AddExercisePrimaryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener{

        fun onBindItems(muscleJoint: MuscleJoint, position: Int){
            itemView.findViewById<TextView>(R.id.txtExerciseMoverName).text = muscleJoint.name
            itemView.setOnClickListener(this)
            itemView.isSelected = isSelected[position]
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            isSelected = Array(muscleJoints.size) {false}
            isSelected[position] = true
            clickListener(muscleJoints[position])
            notifyDataSetChanged()
        }
    }
}