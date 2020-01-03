package com.trainingapp.personaltrainingassistant.ui.schedule

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trainingapp.personaltrainingassistant.objects.Day
import com.trainingapp.personaltrainingassistant.R
import com.trainingapp.personaltrainingassistant.objects.Session
import com.trainingapp.personaltrainingassistant.StaticFunctions

class ScheduleRVAdapter(private val context: Context?, private val day: Day, private val clickListener: (Int) -> Unit, private val longClickListener: (Int) -> Boolean): RecyclerView.Adapter<ScheduleRVAdapter.ScheduleViewHolder>() {

    private var conflicts = ArrayList<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        conflicts = day.getConflicts()
        return ScheduleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.schedule_row, parent, false))
    }

    override fun getItemCount(): Int = day.getSessionCount()

    override fun onBindViewHolder(vh: ScheduleViewHolder, position: Int) {
        vh.onBindItems(day.getSession(position), clickListener, position, longClickListener)
    }

    inner class ScheduleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun onBindItems(session: Session, clickListener: (Int) -> Unit, position: Int, longClickListener: (Int) -> Boolean){
            itemView.findViewById<TextView>(R.id.scheduleRowClientName).text = session.clientName
            itemView.findViewById<TextView>(R.id.scheduleRowTime).text = StaticFunctions.getStrTimeAMPM(session.date, session.duration)
            itemView.setOnClickListener{clickListener(position)}
            itemView.setOnLongClickListener { longClickListener(position) }
            if (conflicts.contains(position))
                itemView.background = context?.getDrawable(R.drawable.border_day_conflict)
        }
    }
}