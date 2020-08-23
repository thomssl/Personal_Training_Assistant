package com.trainingapp.trainingassistant.ui.muscles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.objects.MuscleJoint

/**
 * Adapter to display all muscles found in the database. Displays name of the muscle
 * @param muscles List of all muscles as MuscleJoint objects
 * @param clickListener Function used by MusclesFragment to handle item onClick event (ie edit muscle)
 * @param longClickListener Function used by MusclesFragment to handle item onLongClick event (ie delete muscle)
 */
class MusclesRVAdapter(
    private val muscles: List<MuscleJoint>,
    private val clickListener: (MuscleJoint) -> Unit,
    private val longClickListener: (MuscleJoint, View) -> Boolean
): RecyclerView.Adapter<MusclesRVAdapter.MuscleViewHolder>() {

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