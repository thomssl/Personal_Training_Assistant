package com.trainingapp.personaltrainingassistant.ui.schedule

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.fragment.app.Fragment
import com.trainingapp.personaltrainingassistant.*
import com.trainingapp.personaltrainingassistant.activities.SessionActivity
import com.trainingapp.personaltrainingassistant.database.DatabaseOperations
import com.trainingapp.personaltrainingassistant.objects.Day
import kotlinx.android.synthetic.main.fragment_schedule.*
import java.util.*

class ScheduleFragment : Fragment(), CalendarView.OnDateChangeListener {

    private val calendar: Calendar = Calendar.getInstance()
    private lateinit var databaseOperations: DatabaseOperations
    private lateinit var day: Day

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        calSchedule.setOnDateChangeListener(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        databaseOperations = DatabaseOperations(context)
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
    }

    private fun setAdapter(){
        day = databaseOperations.getScheduleByDay(calendar)
        if (day.getSessionCount() > 0) {
            rvDailySchedule.adapter = ScheduleRVAdapter(context, day,{position -> onItemClick(position)},{position -> onLongItemClick(position)})
            lblScheduleEmpty.visibility = View.INVISIBLE
            rvDailySchedule.visibility = View.VISIBLE
        }
        else {
            lblScheduleEmpty.visibility = View.VISIBLE
            rvDailySchedule.visibility = View.INVISIBLE
        }
    }

    override fun onSelectedDayChange(p0: CalendarView, year: Int, month: Int, day: Int) {
        calendar.set(year,month,day)
        setAdapter()
    }

    private fun onItemClick(position: Int) {
        val intent = Intent(context, SessionActivity::class.java)
        intent.putExtra("client_id", day.getSession(position).clientID)
        intent.putExtra("dayTime", StaticFunctions.getStrDateTime(day.getSession(position).date))
        startActivity(intent)
    }

    private fun onLongItemClick(position: Int): Boolean{
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle(getString(R.string.confirm_delete_session, day.getSession(position).clientName))
        alertDialog.setPositiveButton(R.string.confirm) { _, _ -> databaseOperations.cancelSession(day.getSession(position)); setAdapter()}
        alertDialog.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss()}
        alertDialog.show()
        return true
    }
}