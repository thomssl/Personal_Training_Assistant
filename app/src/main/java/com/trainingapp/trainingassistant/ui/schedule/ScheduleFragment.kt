package com.trainingapp.trainingassistant.ui.schedule

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.StaticFunctions
import com.trainingapp.trainingassistant.activities.SessionActivity
import com.trainingapp.trainingassistant.database.DatabaseOperations2
import com.trainingapp.trainingassistant.objects.Day2
import kotlinx.android.synthetic.main.fragment_schedule.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Fragment called upon from 'Schedule' action within the NavigationDrawer. Used to display, edit and delete sessions
 */
class ScheduleFragment : Fragment(), CalendarView.OnDateChangeListener, CoroutineScope {

    private val calendar: Calendar = Calendar.getInstance()
    private lateinit var databaseOperations: DatabaseOperations2
    private lateinit var day: Day2
    private lateinit var iFragmentToActivity: IFragmentToActivity
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        calSchedule.setOnDateChangeListener(this)//set CalendarView's date change event handler to this (onSelectedDayChange function)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{//if cast successful, this Interface will be used to send date updates to MainActivity
            iFragmentToActivity = context as IFragmentToActivity
        } catch (e: ClassCastException){//if error during cast (cannot cast context to interface) log and notify user
            e.printStackTrace()
            Toast.makeText(context, "Could not cast context as IFragmentToActivity", Toast.LENGTH_LONG).show()
        }
        databaseOperations = DatabaseOperations2(context)
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
    }

    /**
     * UI coroutine to get and set adapter for rvDailySchedule. If no sessions found in the Day, display lblScheduleEmpty
     */
    private fun setAdapter() = launch{
        prgScheduleData.visibility = View.VISIBLE
        val adapter = getAdapter()
        prgScheduleData.visibility = View.GONE
        rvDailySchedule.adapter = adapter
        if (day.getSessionCount() > 0) {
            lblScheduleEmpty.visibility = View.INVISIBLE
            rvDailySchedule.visibility = View.VISIBLE
        }
        else {
            lblScheduleEmpty.visibility = View.VISIBLE
            rvDailySchedule.visibility = View.INVISIBLE
        }
    }

    /**
     * Suspendable IO coroutine to get the day and adapter for that day
     */
    private suspend fun getAdapter() = withContext(Dispatchers.IO){
        day = databaseOperations.getScheduleByDay(calendar)
        ScheduleRVAdapter(context, day,{position -> onItemClick(position)},{position -> onLongItemClick(position)})
    }

    /**
     * Method passed to calSchedule to handle date change event. Updates Fragment calendar and MainActivity calendar then updates session list
     */
    override fun onSelectedDayChange(p0: CalendarView, year: Int, month: Int, day: Int) {
        calendar.set(year,month,day)
        iFragmentToActivity.setCalendarDate(year, month, day)
        setAdapter()
    }

    /**
     * Method passed to rvDailySchedule to handle item onClick event. Opens a SessionActivity with client id, date and time passed through intent
     * @param position index of the session within the Day object
     */
    private fun onItemClick(position: Int) {
        val intent = Intent(context, SessionActivity::class.java)
        intent.putExtra("client_id", day.getSession(position).clientID)
        intent.putExtra("dayTime", StaticFunctions.getStrDateTime(day.getSession(position).date))
        startActivity(intent)
    }

    /**
     * Method passed to rvDailySchedule to handle item onLongClick event. Opens an AlertDialog for the user to confirm session cancellation
     * @param position index of the session within the Day object
     * @return always true since the callback consumed the long click (See Android View.onLongClickListener for more info)
     */
    private fun onLongItemClick(position: Int): Boolean{
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle(getString(R.string.alert_dialog_confirm_removal))
        alertDialog.setMessage(getString(R.string.confirm_delete_session, day.getSession(position).clientName))
        alertDialog.setPositiveButton(R.string.confirm) { _, _ -> databaseOperations.cancelSession(day.getSession(position)); setAdapter()}
        alertDialog.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss()}
        alertDialog.show()
        return true
    }

    interface IFragmentToActivity {
        fun setCalendarDate(year: Int, month: Int, day: Int)
    }
}