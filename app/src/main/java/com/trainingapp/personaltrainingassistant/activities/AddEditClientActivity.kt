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
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

/**
 * Activity to Add a new client or edit an existing client. A client id is sent to the activity through the intent and is used to get a client object.
 * If the client returned is empty (id = 0) no existing information is loaded to the layout and a flag is set to true.
 * If the client returned is populated (id > 0), client parameters are loaded to the layout and a flag is set to false.
 * After collecting or acquiring information the client is inserted or updated depending upon the isNew flag
 */
class AddEditClientActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener {

    private lateinit var databaseOperations: DatabaseOperations
    private var userSettings = ArrayList<Int>()
    private var isNew = false
    private lateinit var intentClient: Client

    /**
     * Creation method overridden to initialize the DatabaseOperations object and set the rules of how the layout changes with user input
     * If a valid client id is sent the information is loaded. Otherwise the flag is set and nothing about the layout changes
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_client)
        setTitle(R.string.edit_client_title)//default is edit client, changed if invalid client id sent

        databaseOperations = DatabaseOperations(this)
        userSettings = databaseOperations.getUserSettings()
        radGrpAddEditClient.setOnCheckedChangeListener(this)
        //each switch is set to follow similar rules. If it becomes checked, make the appropriate views visible and initialize the button text and duration to the default
        //If it becomes unchecked, set views to invisible
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
        //get client from passed client id
        intentClient = databaseOperations.getClient(intent.getIntExtra("id", 0))
        if (intentClient.id == 0){//if client is invalid/blank
            isNew = true//set flag
            setTitle(R.string.add_client_title)//set title to add client
        } else {//if valid client
            btnAddEditClientStartDate.text = intentClient.startDate
            btnAddEditClientEndDate.text = intentClient.endDate
            etxtAddEditClientName.setText(intentClient.name)
            //populate layout depending upon client parameters
            when (intentClient.scheduleType) {
                ScheduleType.NO_SCHEDULE -> {
                    etxtNoScheduleDuration.setText(intentClient.durations[0].toString())//load default duration to field
                    radGrpAddEditClient.check(R.id.radIsNoSchedule)
                }
                ScheduleType.WEEKLY_CONSTANT -> {//use list of views to set appropriate view properties for client
                    val lstDays = ArrayList<Switch>(listOf(swWeeklyConstantIsSunday, swWeeklyConstantIsMonday, swWeeklyConstantIsTuesday, swWeeklyConstantIsWednesday, swWeeklyConstantIsThursday, swWeeklyConstantIsFriday, swWeeklyConstantIsSaturday))
                    val lstTimes = ArrayList<Button>(listOf(btnWeeklyConstantSundayTime, btnWeeklyConstantMondayTime, btnWeeklyConstantTuesdayTime, btnWeeklyConstantWednesdayTime, btnWeeklyConstantThursdayTime, btnWeeklyConstantFridayTime, btnWeeklyConstantSaturdayTime))
                    val lstDurations = ArrayList<EditText>(listOf(etxtWeeklyConstantSundayDuration, etxtWeeklyConstantMondayDuration, etxtWeeklyConstantTuesdayDuration, etxtWeeklyConstantWednesdayDuration, etxtWeeklyConstantThursdayDuration, etxtWeeklyConstantFridayDuration, etxtWeeklyConstantSaturdayDuration))
                    intentClient.days.forEach {//use (it - 1) because days are from 1-7 while indices is from 0-6
                        lstDays[it - 1].isChecked = true
                        lstTimes[it - 1].text = intentClient.getStrTime(intentClient.days.indexOf(it))
                        lstDurations[it - 1].setText(intentClient.durations[intentClient.days.indexOf(it)].toString())
                    }
                    radGrpAddEditClient.check(R.id.radIsWeeklyConst)
                }
                ScheduleType.WEEKLY_VARIABLE -> {
                    etxtWeeklyVariableNumSessions.setText(intentClient.days[0].toString())
                    etxtWeeklyVariableDuration.setText(intentClient.durations[0].toString())
                    radGrpAddEditClient.check(R.id.radIsWeeklyVar)
                }
                ScheduleType.MONTHLY_VARIABLE -> {
                    etxtMonthlyVariableNumSessions.setText(intentClient.days[0].toString())
                    etxtMonthlyVariableDuration.setText(intentClient.durations[0].toString())
                    radGrpAddEditClient.check(R.id.radIsMonthlyVar)
                }
                ScheduleType.BLANK -> finish()//close activity is ScheduleType is invalid
            }
        }
    }

    override fun onCheckedChanged(p0: RadioGroup?, checkedID: Int) {
        when (checkedID){
            //if not NoSchedule, make startDate and endDate views visible. if NoSchedule make views invisible
            //make the right include view visible
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
                btnAddEditClientEndDate.text = getString(R.string.date_format)//reset values for when they become visible again and the value needs to be set again
                lblAddEditClientEndDate.visibility = View.GONE
                btnAddEditClientStartDate.visibility = View.GONE
                btnAddEditClientStartDate.text = getString(R.string.date_format)//reset values for when they become visible again and the value needs to be set again
                lblAddEditClientStartDate.visibility = View.GONE
                incAddEditClientWeeklyConstant.visibility = View.GONE
                incAddEditClientWeeklyVariable.visibility = View.GONE
                incAddEditClientMonthlyVariable.visibility = View.GONE
                incAddEditClientNoSchedule.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Method used by both date buttons to create a DatePickerDialog. Pass listener as a private method to handle the output
     * @param view Button view sent. Passed to updateDate method as button to change the view's text
     */
    fun onDateClick(view: View){
        val dialog = DatePickerDialog(this, R.style.DialogTheme)
        dialog.setOnDateSetListener{_, year: Int, month: Int, day: Int -> updateDate(year, month, day, view as Button)}
        dialog.show()
    }

    /**
     * Method to handle date selection in the startDate and endDate buttons. Sets a calendar with the returned values and assigns the button text with the set date
     * @param year value from DatePickerDialog
     * @param month value from DatePickerDialog
     * @param day value from DatePickerDialog as day of month
     * @param view button to update the date
     */
    private fun updateDate(year: Int, month: Int, day: Int, view: Button){
        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month
        calendar[Calendar.DAY_OF_MONTH] = day
        view.text = StaticFunctions.getStrDate(calendar)
    }

    /**
     * Method used by all time buttons to create a TimePickerDialog. Pass listener as private method to handle the output
     * @param view Button view sent. Passed to updateTime method as button to change view's text
     */
    fun onTimeClick(view: View){
        TimePickerDialog(this, R.style.DialogTheme, {_, hour: Int, minute: Int -> updateTime(hour, minute, view as Button)}, 0, 0, false).show()
    }

    /**
     * Method to handle time selection in all the time buttons. Sets calendar with the returned values and assigns the button text with the set time
     * @param hour value from TimePickerDialog as hour in day (24hour)
     * @param minute value from TimePickerDialog
     * @param view button to update the time
     */
    private fun updateTime(hour: Int, minute: Int, view: Button){
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = hour
        calendar[Calendar.MINUTE] = minute
        view.text = StaticFunctions.getStrTimeAMPM(calendar)
    }

    /**
     * Method to handle the onClick event of btnAddEditConfirm. Collects the data with in the layout and attempts to insert/update the client
     * @param view button view (btnAddEditConfirm)
     */
    fun onClientConfirm(view: View){
        val name = etxtAddEditClientName.text.toString()
        if (!StaticFunctions.badSQLText(name)) {//first check, make sure the name does not contain any illegal characters or isBlank
            val client: Client = when (radGrpAddEditClient.checkedRadioButtonId) {//create Client object depending upon the checked radio button
                R.id.radIsWeeklyConst -> {
                    val startDate = btnAddEditClientStartDate.text.toString()
                    val endDate = btnAddEditClientEndDate.text.toString()
                    if (startDate == getString(R.string.date_format) || endDate == getString(R.string.date_format)){//make sure startDate and endDate are not the default values
                        Snackbar.make(view, "Error. Start date and/or end date was no chosen", Snackbar.LENGTH_LONG).show()
                        return//exit function. Do nothing
                    }
                    val builderDays = StringBuilder()
                    val builderTimes = StringBuilder()
                    val builderDurations = StringBuilder()
                    val lstDays = ArrayList<Switch>(listOf(swWeeklyConstantIsSunday, swWeeklyConstantIsMonday, swWeeklyConstantIsTuesday, swWeeklyConstantIsWednesday, swWeeklyConstantIsThursday, swWeeklyConstantIsFriday, swWeeklyConstantIsSaturday))
                    val lstTimes = ArrayList<Button>(listOf( btnWeeklyConstantSundayTime, btnWeeklyConstantMondayTime, btnWeeklyConstantTuesdayTime, btnWeeklyConstantWednesdayTime, btnWeeklyConstantThursdayTime, btnWeeklyConstantFridayTime, btnWeeklyConstantSaturdayTime))
                    val lstDurations = ArrayList<EditText>(listOf(etxtWeeklyConstantSundayDuration, etxtWeeklyConstantMondayDuration, etxtWeeklyConstantTuesdayDuration, etxtWeeklyConstantWednesdayDuration, etxtWeeklyConstantThursdayDuration, etxtWeeklyConstantFridayDuration, etxtWeeklyConstantSaturdayDuration))
                    for (index in lstDays.indices) {//loop through using the range of 0 -> length of lstDays (ie 0 -> 6)
                        if (lstDays[index].isChecked) {//if day is checked
                            builderDays.append("${index + 1},")//add (index + 1), ie day number
                            val time = lstTimes[index].text.toString()
                            if (time != getString(R.string.time_format))//if time for day is not the default
                                builderTimes.append("${StaticFunctions.getTimeInt(time)},")//add time as minute in day (format for database)
                            else{//if time not set
                                Snackbar.make(view, "Error. One or more of the times for a chosen day was not entered", Snackbar.LENGTH_LONG).show()
                                return//exit function. Do Nothing
                            }
                            val duration = lstDurations[index].text.toString()
                            if (duration.isDigitsOnly()) {//if the duration text is only digit (ie Int). Should always be true due to view's input type
                                if (duration.toInt() in 1..120){//check if duration fits within range of 0 <= duration <= 120. See Wiki
                                    builderDurations.append("${duration.toInt()},")
                                } else {
                                    Snackbar.make(view, "Error. The duration entered is 0 or greater than 120mins. See Wiki for more information", Snackbar.LENGTH_LONG).show()
                                    return//exit function. Do nothing
                                }
                            } else {
                                Snackbar.make(view, "Error. Duration is not an integer", Snackbar.LENGTH_LONG).show()
                                return//exit function. Do nothing
                            }
                        }
                    }
                    if (builderDays.isEmpty()){//if no day selected (no day is checked so builder is still empty)
                        Snackbar.make(view, "Error. No dates selected", Snackbar.LENGTH_LONG).show()
                        return//exit function. Do nothing
                    }
                    //remove the trailing ',' from data
                    builderDays.deleteCharAt(builderDays.lastIndex)
                    builderTimes.deleteCharAt(builderTimes.lastIndex)
                    builderDurations.deleteCharAt(builderDurations.lastIndex)
                    Client(intentClient.id, StaticFunctions.formatForSQL(name), ScheduleType.WEEKLY_CONSTANT, builderDays.toString(), builderTimes.toString(), builderDurations.toString(), startDate, endDate)//construct Client object with collected data
                }
                R.id.radIsWeeklyVar -> {
                    val startDate = btnAddEditClientStartDate.text.toString()
                    val endDate = btnAddEditClientEndDate.text.toString()
                    val duration = etxtWeeklyVariableDuration.text.toString()
                    val sessions = etxtWeeklyVariableNumSessions.text.toString()
                    if (startDate == getString(R.string.date_format) || endDate == getString(R.string.date_format)) {//make sure startDate and endDate are not the default values
                        Snackbar.make(view, "Error. Start date and/or end date was no chosen", Snackbar.LENGTH_LONG).show()
                        return//exit function. Do nothing
                    }
                    if (duration.isDigitsOnly()) {//if the duration text is only digit (ie Int). Should always be true due to view's input type
                        if (duration.toInt() > 120 || duration.toInt() <= 0){//check if duration fits within range of 0 <= duration <= 120. See Wiki
                            Snackbar.make(view, "Error. The duration entered is 0 or greater than 120mins. See Wiki for more information", Snackbar.LENGTH_LONG).show()
                            return//exit function. Do nothing
                        }
                    } else {
                        Snackbar.make(view, "Error. Duration is not an integer", Snackbar.LENGTH_LONG).show()
                        return//exit function. Do nothing
                    }
                    if(sessions.isDigitsOnly()) {//if the # of sessions/week text is only digit (ie Int). Should always be true due to view's input type
                        if (sessions.toInt() > 7 || sessions.toInt() <= 0){//check if # of sessions/week fits within range of 0 < # of sessions/week < 8
                            Snackbar.make(view, "Error. Number of sessions is too high or too low for a single week", Snackbar.LENGTH_LONG).show()
                            return//exit function. Do nothing
                        }
                    } else {
                        Snackbar.make(view, "Error. Number of sessions is not an integer", Snackbar.LENGTH_LONG).show()
                        return//exit function. Do nothing
                    }
                    //construct Client object with collected data. Times is set to "0" as default. if duration is set to 0 the default duration from the user settings is used
                    Client(intentClient.id, StaticFunctions.formatForSQL(name), ScheduleType.WEEKLY_VARIABLE, sessions, "0", if (duration.toInt() == 0) userSettings[0].toString() else duration, startDate, endDate)
                }
                R.id.radIsMonthlyVar -> {
                    val startDate = btnAddEditClientStartDate.text.toString()
                    val endDate = btnAddEditClientEndDate.text.toString()
                    val duration = etxtMonthlyVariableDuration.text.toString()
                    val sessions = etxtMonthlyVariableNumSessions.text.toString()
                    if (startDate == getString(R.string.date_format) || endDate == getString(R.string.date_format)) {//make sure startDate and endDate are not the default values
                        Snackbar.make(view, "Error. Start date and/or end date was no chosen", Snackbar.LENGTH_LONG).show()
                        return//exit function. Do nothing
                    }
                    if (duration.isDigitsOnly()) {//if the duration text is only digit (ie Int). Should always be true due to view's input type
                        if (duration.toInt() > 120 || duration.toInt() <= 0){//check if duration fits within range of 0 <= duration <= 120. See Wiki
                            Snackbar.make(view, "Error. The duration entered is 0 or greater than 120mins. See Wiki for more information", Snackbar.LENGTH_LONG).show()
                            return//exit function. Do nothing
                        }
                    } else {
                        Snackbar.make(view, "Error. Durations is not an integer", Snackbar.LENGTH_LONG).show()
                        return//exit function. Do nothing
                    }
                    if (sessions.isDigitsOnly()) {//if the # of sessions/month text is only digit (ie Int). Should always be true due to view's input type
                        if (sessions.toInt() > 29 || sessions.toInt() <= 0){//check if # of sessions/week fits within range of 0 < # of sessions/week < 30
                            Snackbar.make(view, "Error. Number of sessions is too high or too low for a single month", Snackbar.LENGTH_LONG).show()
                            return//exit function. Do nothing
                        }
                    } else {
                        Snackbar.make(view, "Error. Number of sessions is not an integer", Snackbar.LENGTH_LONG).show()
                        return//exit function. Do nothing
                    }
                    //construct Client object with collected data. Times is set to "0" as default. if duration is set to 0 the default duration from the user settings is used
                    Client(intentClient.id, StaticFunctions.formatForSQL(name), ScheduleType.MONTHLY_VARIABLE, sessions, "0", if (duration.toInt() == 0) userSettings[0].toString() else duration, startDate, endDate)
                }
                R.id.radIsNoSchedule -> {
                    val duration = etxtNoScheduleDuration.text.toString()
                    if (duration.isDigitsOnly()){//if the duration text is only digit (ie Int). Should always be true due to view's input type
                        if (duration.toInt() > 120 || duration.toInt() <= 0){//check if duration fits within range of 0 <= duration <= 120. See Wiki
                            Snackbar.make(view, "Error. The duration entered is 0 or greater than 120mins. See Wiki for more information", Snackbar.LENGTH_LONG).show()
                            return//exit function. Do nothing
                        }
                    } else {
                        Snackbar.make(view, "Error. Duration is not an integer", Snackbar.LENGTH_LONG).show()
                        return//exit function. Do nothing
                    }
                    //construct Client object with collected data. Days, Times, StartDate and EndDate are set to "0" as default. if duration is set to 0 the default duration from the user settings is used
                    Client(0, StaticFunctions.formatForSQL(name), ScheduleType.NO_SCHEDULE, "0", "0", if (duration.toInt() == 0) userSettings[0].toString() else duration, "0", "0")
                }
                else -> {
                    //if somehow no radio button is selected a blank client is passed forward
                    Client(intentClient.id, "", ScheduleType.NO_SCHEDULE, "", "", "", "", "")
                }
            }
            if (client.name != "") {//if the blank client has moved forward, it will be ignored
                //get conflicts with other existing clients. String will be empty if no conflicts and filled with conflict names if conflicts are found
                val conflicts = if (client.scheduleType == ScheduleType.WEEKLY_CONSTANT) databaseOperations.checkClientConflict(client, if (isNew) -1 else client.id) else ""
                if (conflicts.isEmpty()) {//if no conflicts found
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
            Snackbar.make(view, if (name.isBlank()) "Name is empty" else "Invalid input character inside client name. See Wiki for more information", Snackbar.LENGTH_LONG).show()
        }
    }
}
