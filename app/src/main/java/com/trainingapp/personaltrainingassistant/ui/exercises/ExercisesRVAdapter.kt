package com.trainingapp.personaltrainingassistant.ui.exercises

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trainingapp.personaltrainingassistant.objects.Exercise
import com.trainingapp.personaltrainingassistant.R

/**
 * Adapter to display all exercises found in the database. Displays all pertinent information about the exercise
 * @param exercises List of all exercises as Exercise objects
 * @param clickListener Function used by ExercisesFragment to handle item onClick event (ie edit exercise)
 * @param longClickListener Function used by ExercisesFragment to handle item onLongClick event (ie delete exercise)
 */
class ExercisesRVAdapter(private val exercises: ArrayList<Exercise>, private val clickListener: (Int) -> Unit, private val longClickListener: (Exercise) -> Boolean): RecyclerView.Adapter<ExercisesRVAdapter.ExerciseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        return ExerciseViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.exercises_row, parent, false))
    }

    override fun getItemCount(): Int = exercises.size

    override fun onBindViewHolder(vh: ExerciseViewHolder, position: Int) {
        vh.onBindItems(exercises[position], clickListener, longClickListener)
    }

    inner class ExerciseViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun onBindItems(exercise: Exercise, clickListener: (Int) -> Unit, longClickListener: (Exercise) -> Boolean){
            itemView.findViewById<TextView>(R.id.txtExercisesRowName).text = exercise.name
            itemView.findViewById<TextView>(R.id.txtExercisesRowType).text = exercise.type.text
            itemView.findViewById<TextView>(R.id.txtExercisesRowPrimaryMover).text = exercise.primaryMover.name
            itemView.findViewById<TextView>(R.id.txtExercisesRowSecondaryMovers).text = exercise.getSecondaryMoversNames()
            itemView.setOnClickListener { clickListener(exercise.id) }
            itemView.setOnLongClickListener { longClickListener(exercise) }
        }
    }
}