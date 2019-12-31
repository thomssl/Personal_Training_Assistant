package com.trainingapp.personaltrainingassistant.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Switch
import androidx.core.text.isDigitsOnly
import com.google.android.material.snackbar.Snackbar
import com.trainingapp.personaltrainingassistant.R
import com.trainingapp.personaltrainingassistant.StaticFunctions
import com.trainingapp.personaltrainingassistant.database.DatabaseOperations
import com.trainingapp.personaltrainingassistant.enumerators.ScheduleType
import com.trainingapp.personaltrainingassistant.objects.Client
import kotlinx.android.synthetic.main.activity_add_edit_client.*
import kotlinx.android.synthetic.main.monthly_variable.*
import kotlinx.android.synthetic.main.no_schedule.*
import kotlinx.android.synthetic.main.weekly_constant.*
import kotlinx.android.synthetic.main.weekly_variable.*
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class AddEditClientActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener {

    private lateinit var databaseOperations: DatabaseOperations
    private var userSettings = ArrayList<Int>()
    private var isNew = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_client)
        setTitle(R.string.edit_client_title)

        databaseOperations = DatabaseOperations(this)
        userSettings = databaseOperations.getUserSettings()
        radGrpAddEditClient.setOnCheckedChangeListener(this)
        swWeeklyConstantIsMonday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                lblWeeklyConstantMondayDuration.visibility = View.VISIBLE
                btnWeeklyConstantMondayTime.visibility = View.VISIBLE
                btnWeeklyConstantMondayTime.text = getString(R.string.time_format)
                etxtWeeklyConstantMondayDuration.visibility = View.VISIBLE
                etxtWeeklyConstantMondayDuration.setText(userSettings[0].toString())
            } else {
                lblWeeklyConstantMondayDuration.visibility = View.INVISIBLE
                btnWeeklyConstantMondayTime.visibility = View.INVISIBLE
                etxtWeeklyConstantMondayDuration.visibility = View.INVISIBLE
            }
        }
        swWeeklyConstantIsTuesday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                lblWeeklyConstantTuesdayDuration.visibility = View.VISIBLE
                btnWeeklyConstantTuesdayTime.visibility = View.VISIBLE
                btnWeeklyConstantTuesdayTime.text = getString(R.string.time_format)
                etxtWeeklyConstantTuesdayDuration.visibility = View.VISIBLE
                etxtWeeklyConstantTuesdayDuration.setText(userSettings[0].toString())
            } else {
                lblWeeklyConstantTuesdayDuration.visibility = View.INVISIBLE
                btnWeeklyConstantTuesdayTime.visibility = View.INVISIBLE
                etxtWeeklyConstantTuesdayDuration.visibility = View.INVISIBLE
            }
        }
        swWeeklyConstantIsWednesday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                lblWeeklyConstantWednesdayDuration.visibility = View.VISIBLE
                btnWeeklyConstantWednesdayTime.visibility = View.VISIBLE
                btnWeeklyConstantWednesdayTime.text = getString(R.string.time_format)
                etxtWeeklyConstantWednesdayDuration.visibility = View.VISIBLE
                etxtWeeklyConstantWednesdayDuration.setText(userSettings[0].toString())
            } else {
                lblWeeklyConstantWednesdayDuration.visibility = View.INVISIBLE
                btnWeeklyConstantWednesdayTime.visibility = View.INVISIBLE
                etxtWeeklyConstantWednesdayDuration.visibility = View.INVISIBLE
            }
        }
        swWeeklyConstantIsThursday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                lblWeeklyConstantThursdayDuration.visibility = View.VISIBLE
                btnWeeklyConstantThursdayTime.visibility = View.VISIBLE
                btnWeeklyConstantThursdayTime.text = getString(R.string.time_format)
                etxtWeeklyConstantThursdayDuration.visibility = View.VISIBLE
                etxtWeeklyConstantThursdayDuration.setText(userSettings[0].toString())
            } else {
                lblWeeklyConstantThursdayDuration.visibility = View.INVISIBLE
                btnWeeklyConstantThursdayTime.visibility = View.INVISIBLE
                etxtWeeklyConstantThursdayDuration.visibility = View.INVISIBLE
            }
        }
        swWeeklyConstantIsFriday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                lblWeeklyConstantFridayDuration.visibility = View.VISIBLE
                btnWeeklyConstantFridayTime.visibility = View.VISIBLE
                btnWeeklyConstantFridayTime.text = getString(R.string.time_format)
                etxtWeeklyConstantFridayDuration.visibility = View.VISIBLE
                etxtWeeklyConstantFridayDuration.setText(userSettings[0].toString())
            } else {
                lblWeeklyConstantFridayDuration.visibility = View.INVISIBLE
                btnWeeklyConstantFridayTime.visibility = View.INVISIBLE
                etxtWeeklyConstantFridayDuration.visibility = View.INVISIBLE
            }
        }
        swWeeklyConstantIsSaturday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                lblWeeklyConstantSaturdayDuration.visibility = View.VISIBLE
                btnWeeklyConstantSaturdayTime.visibility = View.VISIBLE
                btnWeeklyConstantSaturdayTime.text = getString(R.string.time_format)
                etxtWeeklyConstantSaturdayDuration.visibility = View.VISIBLE
                etxtWeeklyConstantSaturdayDuration.setText(userSettings[0].toString())
            } else {
                lblWeeklyConstantSaturdayDuration.visibility = View.INVISIBLE
                btnWeeklyConstantSaturdayTime.visibility = View.INVISIBLE
                etxtWeeklyConstantSaturdayDuration.visibility = View.INVISIBLE
            }
        }
        swWeeklyConstantIsSunday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                lblWeeklyConstantSundayDuration.visibility = View.VISIBLE
                btnWeeklyConstantSundayTime.visibility = View.VISIBLE
                btnWeeklyConstantSundayTime.text = getString(R.string.time_format)
                etxtWeeklyConstantSundayDuration.visibility = View.VISIBLE
                etxtWeeklyConstantSundayDuration.setText(userSettings[0].toString())
            } else {
                lblWeeklyConstantSundayDuration.visibility = View.INVISIBLE
                btnWeeklyConstantSundayTime.visibility = View.INVISIBLE
                etxtWeeklyConstantSundayDuration.visibility = View.INVISIBLE
            }
        }
        val client = databaseOperations.getClient(intent.getIntExtra("id", 0))
        if (client.id == 0){
            isNew = true
            setTitle(R.string.add_client_title)
        } else {
            btnAddEditClientStartDate.text = client.startDate
            btnAddEditClientEndDate.text = client.endDate
            etxtAddEditClientName.setText(client.name)
            when (client.scheduleType) {
                ScheduleType.NO_SCHEDULE -> {
                    etxtNoScheduleDuration.setText(client.durations[0].toString())
                    radGrpAddEditClient.check(R.id.radIsNoSchedule)
                }
                ScheduleType.WEEKLY_CONSTANT -> {
                    val lstDays = ArrayList<Switch>(listOf(swWeeklyConstantIsSunday, swWeeklyConstantIsMonday, swWeeklyConstantIsTuesday, swWeeklyConstantIsWednesday, swWeeklyConstantIsThursday, swWeeklyConstantIsFriday, swWeeklyConstantIsSaturday))
                    val lstTimes = ArrayList<Button>(listOf(btnWeeklyConstantSundayTime, btnWeeklyConstantMondayTime, btnWeeklyConstantTuesdayTime, btnWeeklyConstantWednesdayTime, btnWeeklyConstantThursdayTime, btnWeeklyConstantFridayTime, btnWeeklyConstantSaturdayTime))
                    val lstDurations = ArrayList<EditText>(listOf(etxtWeeklyConstantSundayDuration, etxtWeeklyConstantMondayDuration, etxtWeeklyConstantTuesdayDuration, etxtWeeklyConstantWednesdayDuration, etxtWeeklyConstantThursdayDuration, etxtWeeklyConstantFridayDuration, etxtWeeklyConstantSaturdayDuration))
                    client.days.forEach {
                        lstDays[it - 1].isChecked = true
                        lstTimes[it - 1].text = client.getStrTime(client.days.indexOf(it))
                        lstDurations[it - 1].setText(client.durations[client.days.indexOf(it)].toString())
                    }
                    radGrpAddEditClient.check(R.id.radIsWeeklyConst)
                }
                ScheduleType.WEEKLY_VARIABLE -> {
                    etxtWeeklyVariableNumSessions.setText(client.days[0].toString())
                    etxtWeeklyVariableDuration.setText(client.durations[0].toString())
                    radGrpAddEditClient.check(R.id.radIsWeeklyVar)
                }
                ScheduleType.MONTHLY_VARIABLE -> {
                    etxtMonthlyVariableNumSessions.setText(client.days[0].toString())
                    etxtMonthlyVariableDuration.setText(client.durations[0].toString())
                    radGrpAddEditClient.check(R.id.radIsMonthlyVar)
                }
                ScheduleType.BLANK -> finish()
            }
        }
    }

    override fun onCheckedChanged(p0: RadioGroup?, checkedID: Int) {
        when (checkedID){
            R.id.radIsWeeklyConst -> {
                btnAddEditClientEndDate.visibility = View.VISIBLE
                lblAddEditClientEndDate.visibility = View.VISIBLE
                btnAddEditClientStartDate.visibility = View.VISIBLE
                lblAddEditClientStartDate.visibility = View.VISIBLE
                incAddEditClientWeeklyConstant.visibility = View.VISIBLE
                incAddEditClientWeeklyVariable.visibility = View.GONE
                incAddEditClientMonthlyVariable.visibility = View.GONE
                incAddEditClientNoSchedule.visibility = View.GONE
            }
            R.id.radIsWeeklyVar -> {
                etxtWeeklyVariableDuration.setText(userSettings[0].toString())
                btnAddEditClientEndDate.visibility = View.VISIBLE
                lblAddEditClientEndDate.visibility = View.VISIBLE
                btnAddEditClientStartDate.visibility = View.VISIBLE
                lblAddEditClientStartDate.visibility = View.VISIBLE
                incAddEditClientWeeklyConstant.visibility = View.GONE
                incAddEditClientWeeklyVariable.visibility = View.VISIBLE
                incAddEditClientMonthlyVariable.visibility = View.GONE
                incAddEditClientNoSchedule.visibility = View.GONE
            }
            R.id.radIsMonthlyVar -> {
                etxtMonthlyVariableDuration.setText(userSettings[0].toString())
                btnAddEditClientEndDate.visibility = View.VISIBLE
                lblAddEditClientEndDate.visibility = View.VISIBLE
                btnAddEditClientStartDate.visibility = View.VISIBLE
                lblAddEditClientStartDate.visibility = View.VISIBLE
                incAddEditClientWeeklyConstant.visibility = View.GONE
                incAddEditClientWeeklyVariable.visibility = View.GONE
                incAddEditClientMonthlyVariable.visibility = View.VISIBLE
                incAddEditClientNoSchedule.visibility = View.GONE
            }
            R.id.radIsNoSchedule -> {
                btnAddEditClientEndDate.visibility = View.GONE
                btnAddEditClientEndDate.text = getString(R.string.date_format)
                lblAddEditClientEndDate.visibility = View.GONE
                btnAddEditClientStartDate.visibility = View.GONE
                btnAddEditClientStartDate.text = getString(R.string.date_format)
                lblAddEditClientStartDate.visibility = View.GONE
                incAddEditClientWeeklyConstant.visibility = View.GONE
                incAddEditClientWeeklyVariable.visibility = View.GONE
                incAddEditClientMonthlyVariable.visibility = View.GONE
                incAddEditClientNoSchedule.visibility = View.VISIBLE
            }
        }
    }

    fun onDateClick(view: View){
        val dialog = DatePickerDialog(this, R.style.DialogTheme)
        dialog.setOnDateSetListener{_, year: Int, month: Int, day: Int -> updateDate(year, month, day, view as Button)}
        dialog.show()
    }

    private fun updateDate(year: Int, month: Int, day: Int, view: Button){
        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month
        calendar[Calendar.DAY_OF_MONTH] = day
        view.text = StaticFunctions.getStrDate(calendar)
    }

    fun onTimeClick(view: View){
        TimePickerDialog(this, R.style.DialogTheme, {_, hour: Int, minute: Int -> updateTime(hour, minute, view as Button)}, 0, 0, false).show()
    }

    private fun updateTime(hour: Int, minute: Int, view: Button){
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = hour
        calendar[Calendar.MINUTE] = minute
        view.text = StaticFunctions.getStrTimeAMPM(calendar)
    }

    fun onClientConfirm(view: View){
        val name = etxtAddEditClientName.text.toString()
        if (!StaticFunctions.badSQLText(name) && name.isNotBlank()) {
            val client: Client = when (radGrpAddEditClient.checkedRadioButtonId) {
                R.id.radIsWeeklyConst -> {
                    val startDate = btnAddEditClientStartDate.text.toString()
                    val endDate = btnAddEditClientEndDate.text.toString()
                    if (startDate == getString(R.string.date_format) || endDate == getString(R.string.date_format)){
                        Snackbar.make(view, "Error. Start date and/or end date was no chosen", Snackbar.LENGTH_LONG).show()
                        return
                    }
                    val builderDays = StringBuilder()
                    val builderTimes = StringBuilder()
                    val builderDurations = StringBuilder()
                    val lstDays = ArrayList<Switch>(listOf(swWeeklyConstantIsSunday, swWeeklyConstantIsMonday, swWeeklyConstantIsTuesday, swWeeklyConstantIsWednesday, swWeeklyConstantIsThursday, swWeeklyConstantIsFriday, swWeeklyConstantIsSaturday))
                    val lstTimes = ArrayList<Button>(listOf( btnWeeklyConstantSundayTime, btnWeeklyConstantMondayTime, btnWeeklyConstantTuesdayTime, btnWeeklyConstantWednesdayTime, btnWeeklyConstantThursdayTime, btnWeeklyConstantFridayTime, btnWeeklyConstantSaturdayTime))
                    val lstDurations = ArrayList<EditText>(listOf(etxtWeeklyConstantSundayDuration, etxtWeeklyConstantMondayDuration, etxtWeeklyConstantTuesdayDuration, etxtWeeklyConstantWednesdayDuration, etxtWeeklyConstantThursdayDuration, etxtWeeklyConstantFridayDuration, etxtWeeklyConstantSaturdayDuration))
                    for (index in lstDays.indices) {
                        if (lstDays[index].isChecked) {
                            builderDays.append("${index + 1},")
                            val time = lstTimes[index].text.toString()
                            if (time != getString(R.string.time_format))
                                builderTimes.append("${StaticFunctions.getTimeInt(time)},")
                            else{
                                Snackbar.make(view, "Error. One or more of the times for a chosen day was not entered", Snackbar.LENGTH_LONG).show()
                                return
                            }
                            try {
                                val duration = lstDurations[index].text.toString().toInt()
                                if (duration > 120 || duration <= 0){
                                    Snackbar.make(view, "Error. One of the durations entered is 0 or greater than 120mins. See Wiki for more information", Snackbar.LENGTH_LONG).show()
                                    return
                                } else
                                    builderDurations.append("${duration},")
                            } catch (e: Exception){
                                e.printStackTrace()
                                Snackbar.make(view, "Error. One of the durations entered is not an Integer", Snackbar.LENGTH_LONG).show()
                                return
                            }
                        }
                    }
                    if (builderDays.isEmpty()){
                        Snackbar.make(view, "Error. No dates selected", Snackbar.LENGTH_LONG).show()
                        return
                    }
                    builderDays.deleteCharAt(builderDays.lastIndex)
                    builderTimes.deleteCharAt(builderTimes.lastIndex)
                    builderDurations.deleteCharAt(builderDurations.lastIndex)
                    Client(0, StaticFunctions.formatForSQL(name), ScheduleType.WEEKLY_CONSTANT, builderDays.toString(), builderTimes.toString(), builderDurations.toString(), startDate, endDate)
                }
                R.id.radIsWeeklyVar -> {
                    val startDate = btnAddEditClientStartDate.text.toString()
                    val endDate = btnAddEditClientEndDate.text.toString()
                    val duration = etxtWeeklyVariableDuration.text.toString()
                    val sessions = etxtWeeklyVariableNumSessions.text.toString()
                    if (startDate == getString(R.string.date_format) || endDate == getString(R.string.date_format)) {
                        Snackbar.make(view, "Error. Start date and/or end date was no chosen", Snackbar.LENGTH_LONG).show()
                        return
                    }
                    if (duration.isDigitsOnly()) {
                        if (duration.toInt() > 120 || duration.toInt() <= 0){
                            Snackbar.make(view, "Error. The duration entered is 0 or greater than 120mins. See Wiki for more information", Snackbar.LENGTH_LONG).show()
                            return
                        }
                    } else {
                        Snackbar.make(view, "Error. Duration is not an integer", Snackbar.LENGTH_LONG).show()
                        return
                    }
                    if(sessions.isDigitsOnly()) {
                        if (sessions.toInt() > 7 || sessions.toInt() <= 0){
                            Snackbar.make(view, "Error. Number of sessions is too high or too low for a single week", Snackbar.LENGTH_LONG).show()
                            return
                        }
                    } else {
                        Snackbar.make(view, "Error. Number of sessions is not an integer", Snackbar.LENGTH_LONG).show()
                        return
                    }
                    Client(0, StaticFunctions.formatForSQL(name), ScheduleType.WEEKLY_VARIABLE, sessions, "0", if (duration.toInt() == 0) userSettings[0].toString() else duration, startDate, endDate)
                }
                R.id.radIsMonthlyVar -> {
                    val startDate = btnAddEditClientStartDate.text.toString()
                    val endDate = btnAddEditClientEndDate.text.toString()
                    val duration = etxtMonthlyVariableDuration.text.toString()
                    val sessions = etxtMonthlyVariableNumSessions.text.toString()
                    if (startDate == getString(R.string.date_format) || endDate == getString(R.string.date_format)) {
                        Snackbar.make(view, "Error. Start date and/or end date was no chosen", Snackbar.LENGTH_LONG).show()
                        return
                    }
                    if (duration.isDigitsOnly()) {
                        if (duration.toInt() > 120 || duration.toInt() <= 0){
                            Snackbar.make(view, "Error. The duration entered is 0 or greater than 120mins. See Wiki for more information", Snackbar.LENGTH_LONG).show()
                            return
                        }
                    } else {
                        Snackbar.make(view, "Error. Durations is not an integer", Snackbar.LENGTH_LONG).show()
                        return
                    }
                    if (sessions.isDigitsOnly()) {
                        if (sessions.toInt() > 29 || sessions.toInt() <= 0){
                            Snackbar.make(view, "Error. Number of sessions is too high or too low for a single month", Snackbar.LENGTH_LONG).show()
                            return
                        }
                    } else {
                        Snackbar.make(view, "Error. Number of sessions is not an integer", Snackbar.LENGTH_LONG).show()
                        return
                    }
                    Client(0, StaticFunctions.formatForSQL(name), ScheduleType.MONTHLY_VARIABLE, sessions, "0", if (duration.toInt() == 0) userSettings[0].toString() else duration, startDate, endDate)
                }
                R.id.radIsNoSchedule -> {
                    val duration = etxtNoScheduleDuration.text.toString()
                    if (duration.isDigitsOnly()){
                        if (duration.toInt() > 120 || duration.toInt() <= 0){
                            Snackbar.make(view, "Error. The duration entered is 0 or greater than 120mins. See Wiki for more information", Snackbar.LENGTH_LONG).show()
                            return
                        }
                    } else {
                        Snackbar.make(view, "Error. Duration is not an integer", Snackbar.LENGTH_LONG).show()
                        return
                    }
                    Client(0, StaticFunctions.formatForSQL(name), ScheduleType.NO_SCHEDULE, "0", "0", if (duration.toInt() == 0) userSettings[0].toString() else duration, "0", "0")
                }
                else -> {
                    Client(0, "", ScheduleType.NO_SCHEDULE, "", "", "", "", "")
                }
            }
            if (client.name != "") {
                val conflicts = if (client.scheduleType == ScheduleType.WEEKLY_CONSTANT) databaseOperations.checkClientConflict(client) else ""
                if (conflicts.isEmpty()) {
                    if (isNew) {
                        if (databaseOperations.insertClient(client)) {
                            Snackbar.make(view, "Inserted new client", Snackbar.LENGTH_LONG).show()
                            finish()
                        } else
                            Snackbar.make(view, "SQL Error inserting new client", Snackbar.LENGTH_LONG).show()
                    } else {
                        if (databaseOperations.updateClient(client)) {
                            Snackbar.make(view, "Updated client", Snackbar.LENGTH_LONG).show()
                            finish()
                        } else
                            Snackbar.make(view, "SQL Error updating client", Snackbar.LENGTH_LONG).show()
                    }
                } else
                    Snackbar.make(view, "Error. Conflict with $conflicts", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(view, "Error. No radio button selected", Snackbar.LENGTH_LONG).show()
            }
        } else {
            Snackbar.make(view, if (name.isEmpty()) "Name is empty" else "Invalid input character inside client name. See Wiki for more information", Snackbar.LENGTH_LONG).show()
        }
    }
}
