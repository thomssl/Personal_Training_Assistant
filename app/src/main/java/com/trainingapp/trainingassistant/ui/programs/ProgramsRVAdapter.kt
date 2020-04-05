package com.trainingapp.trainingassistant.ui.programs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.objects.Program

/**
 * Adapter to display all programs found in the database. Displays all pertinent information about the program
 * @param programs List of all programs as Program objects
 * @param clickListener Function used by ProgramsFragment to handle item onClick event (ie edit program)
 * @param longClickListener Function used by ProgramsFragment to handle item onLongClick event (ie delete program)
 */
class ProgramsRVAdapter(private val programs: ArrayList<Program>, private val clickListener: (Int) -> Unit, private val longClickListener: (Program) -> Boolean): RecyclerView.Adapter<ProgramsRVAdapter.ProgramViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramViewHolder {
        return ProgramViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.program_row, parent, false))
    }

    override fun getItemCount(): Int = programs.size

    override fun onBindViewHolder(vh: ProgramViewHolder, position: Int) {
        vh.onBindItems(programs[position], clickListener, longClickListener)
    }

    inner class ProgramViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun onBindItems(program: Program, clickListener: (Int) -> Unit, longClickListener: (Program) -> Boolean){
            itemView.findViewById<TextView>(R.id.txtProgramsRowName).text = program.name
            itemView.findViewById<TextView>(R.id.txtProgramsRowDays).text = program.days.toString()
            itemView.findViewById<TextView>(R.id.txtProgramsRowExercisesCount).text = program.exercises.size.toString()
            itemView.findViewById<TextView>(R.id.txtProgramsRowDescription).text = program.desc
            itemView.setOnClickListener { clickListener(program.id) }
            itemView.setOnLongClickListener { longClickListener(program) }
        }
    }
}