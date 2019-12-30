package com.trainingapp.personaltrainingassistant.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trainingapp.personaltrainingassistant.objects.ExerciseSession
import com.trainingapp.personaltrainingassistant.R
import com.trainingapp.personaltrainingassistant.objects.Session

class SessionExercisesRVAdapter(var session: Session, private val clickListener: (ExerciseSession, Int) -> Unit, private val longClickListener: (ExerciseSession) -> Boolean) : RecyclerView.Adapter<SessionExercisesRVAdapter.SessionExercisesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionExercisesViewHolder {
        return SessionExercisesViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.session_exercise_row, parent, false))
    }

    override fun getItemCount(): Int = session.getExerciseCount()

    override fun onBindViewHolder(vh: SessionExercisesViewHolder, position: Int) {
        vh.onBindItems(session.getExercise(position), clickListener, position, longClickListener)
    }

    inner class SessionExercisesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun onBindItems(exerciseSession: ExerciseSession, clickListener: (ExerciseSession, Int) -> Unit, position: Int, longClickListener: (ExerciseSession) -> Boolean){
            itemView.findViewById<TextView>(R.id.txtSessionExerciseOrder).text = exerciseSession.order.toString()
            itemView.findViewById<TextView>(R.id.txtSessionExerciseName).text = exerciseSession.name
            itemView.findViewById<TextView>(R.id.txtSessionExerciseResistance).text = exerciseSession.resistance
            itemView.findViewById<TextView>(R.id.txtSessionExerciseSets).text = exerciseSession.sets
            itemView.findViewById<TextView>(R.id.txtSessionExerciseReps).text = exerciseSession.reps
            itemView.setOnClickListener{clickListener(exerciseSession, position)}
            itemView.setOnLongClickListener {longClickListener(exerciseSession)}
        }
    }
}