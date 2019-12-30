package com.trainingapp.personaltrainingassistant.ui.muscles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trainingapp.personaltrainingassistant.objects.MuscleJoint
import com.trainingapp.personaltrainingassistant.R

class MusclesRVAdapter(private val muscles: ArrayList<MuscleJoint>, private val clickListener: (MuscleJoint) -> Unit, private val longClickListener: (MuscleJoint, View) -> Boolean): RecyclerView.Adapter<MusclesRVAdapter.MuscleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MuscleViewHolder {
        return MuscleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.muscles_row, parent, false))
    }

    override fun getItemCount(): Int = muscles.size

    override fun onBindViewHolder(vh: MuscleViewHolder, position: Int) {
        vh.onBindItems(muscles[position], clickListener, longClickListener)
    }

    inner class MuscleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun onBindItems(muscle: MuscleJoint, clickListener: (MuscleJoint) -> Unit, longClickListener: (MuscleJoint, View) -> Boolean){
            itemView.findViewById<TextView>(R.id.txtMusclesRowName).text = muscle.name
            itemView.setOnClickListener { clickListener(muscle) }
            itemView.setOnLongClickListener { longClickListener(muscle, it) }
        }
    }
}