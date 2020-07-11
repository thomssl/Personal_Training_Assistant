package com.trainingapp.trainingassistant.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.objects.ExerciseProgram
import com.trainingapp.trainingassistant.objects.Program

/**
 * Adapter to display the ExercisePrograms found within a given Program
 * @param program Program displayed in AddEditProgramActivity
 * @param clickListener Function from AddEditProgramActivity to handle the onClick event of an item
 * @param longClickListener Function from AddEditProgramActivity to handle the onLongClick event of an item
 */
class ProgramExercisesRVAdapter(
    var program: Program,
    private val clickListener: (ExerciseProgram, Int) -> Unit,
    private val longClickListener: (ExerciseProgram) -> Boolean
) : RecyclerView.Adapter<ProgramExercisesRVAdapter.ProgramExercisesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramExercisesViewHolder {
        return ProgramExercisesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.program_exercise_row, parent, false))
    }

    override fun getItemCount(): Int = program.exerciseCount

    override fun onBindViewHolder(vh: ProgramExercisesViewHolder, position: Int) {
        vh.onBindItems(program.getExercise(position), clickListener, position, longClickListener)
    }

    inner class ProgramExercisesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun onBindItems(exerciseProgram: ExerciseProgram, clickListener: (ExerciseProgram, Int) -> Unit, position: Int, longClickListener: (ExerciseProgram) -> Boolean){
            itemView.findViewById<TextView>(R.id.txtProgramExerciseOrder).text = exerciseProgram.order.toString()
            itemView.findViewById<TextView>(R.id.txtProgramExerciseName).text = exerciseProgram.name
            itemView.findViewById<TextView>(R.id.txtProgramExerciseDays).text = exerciseProgram.day.toString()
            itemView.findViewById<TextView>(R.id.txtProgramExerciseSets).text = exerciseProgram.sets
            itemView.findViewById<TextView>(R.id.txtProgramExerciseReps).text = exerciseProgram.reps
            itemView.setOnClickListener{clickListener(exerciseProgram, position)}
            itemView.setOnLongClickListener {longClickListener(exerciseProgram)}
        }
    }
}