package com.trainingapp.trainingassistant.ui.schedule

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.StaticFunctions
import com.trainingapp.trainingassistant.objects.Day
import com.trainingapp.trainingassistant.objects.Session

/**
 * Adapter to display all session found for a given day. Displays client name and session time. Highlights conflicts found between sessions
 * @param context context from the ScheduleFragment. Used to get drawables
 * @param day Day object that contains the list of sessions to display
 * @param clickListener Function used by ScheduleFragment to handle item onClick event (ie edit session)
 * @param longClickListener Function used by ScheduleFragment to handle item onLongClick event (ie cancel session)
 */
class ScheduleRVAdapter(private val context: Context?, private val day: Day, private val clickListener: (Int) -> Unit, private val longClickListener: (Int) -> Boolean): RecyclerView.Adapter<ScheduleRVAdapter.ScheduleViewHolder>() {

    private lateinit var conflicts: List<Int>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        conflicts = day.conflicts//fills list with sessions indices that have a conflict with another session
        return ScheduleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.schedule_row, parent, false))
    }

    override fun getItemCount(): Int = day.sessionCount

    override fun onBindViewHolder(vh: ScheduleViewHolder, position: Int) {
        vh.onBindItems(day.getSession(position), clickListener, position, longClickListener)
    }

    inner class ScheduleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun onBindItems(session: Session, clickListener: (Int) -> Unit, position: Int, longClickListener: (Int) -> Boolean){
            itemView.findViewById<TextView>(R.id.scheduleRowClientName).text = session.clientName
            itemView.findViewById<TextView>(R.id.scheduleRowTime).text = StaticFunctions.getStrTimeAMPM(session.date, session.duration)
            itemView.setOnClickListener{clickListener(position)}
            itemView.setOnLongClickListener { longClickListener(position) }
            if (conflicts.contains(position))//if this session index was flagged as a conflict set background to be highlighted
                itemView.background = context?.getDrawable(R.drawable.border_day_conflict)
        }
    }
}