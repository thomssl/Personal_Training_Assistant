package com.trainingapp.trainingassistant.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import com.google.android.material.snackbar.Snackbar
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.StaticFunctions
import com.trainingapp.trainingassistant.database.DatabaseOperations
import com.trainingapp.trainingassistant.enumerators.ScheduleType
import com.trainingapp.trainingassistant.objects.Client
import com.trainingapp.trainingassistant.objects.Schedule
import kotlinx.android.synthetic.main.activity_add_edit_client.*
import kotlinx.android.synthetic.main.monthly_variable.*
import kotlinx.android.synthetic.main.no_schedule.*
import kotlinx.android.synthetic.main.weekly_constant.*
import kotlinx.android.synthetic.main.weekly_variable.*
import java.util.*

/**
 * Activity to Add a new client or edit an existing client. A client id is sent to the activity through the intent and is used to get a client object.
 * If the client returned is empty (id = 0) no existing information is loaded to the layout and a flag is set to true.
 * If the client returned is populated (id > 0), client parameters are loaded to the layout and a flag is set to false.
 * After collecting or acquiring information the client is inserted or updated depending upon the isNew flag
 */
class AddEditClientActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener {

    private lateinit var databaseOperations: DatabaseOperations
    private lateinit var userSettings: List<Int>
    private var isNew = false
    private lateinit var intentClient: Client
    private lateinit var defaultDuration: String
    private lateinit var lstDays: List<Switch>
    private lateinit var lstTimes: List<Button>
    private lateinit var lstDurations: List<EditText>
    private lateinit var lstLabels: List<TextView>

    /**
     * Creation method overridden to initialize the DatabaseOperations object and set the rules of how the layout changes with user input
     * If a valid client id is sent the information is loaded. Otherwise the flag is set and nothing about the layout changes
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_client)
        // Default is edit client, changed if invalid client id sent
        setTitle(R.string.edit_client_title)

        lstDays = listOf(
            swWeeklyConstantIsSunday,
            swWeeklyConstantIsMonday,
            swWeeklyConstantIsTuesday,
            swWeeklyConstantIsWednesday,
            swWeeklyConstantIsThursday,
            swWeeklyConstantIsFriday,
            swWeeklyConstantIsSaturday
        )
        lstLabels = listOf(
            lblWeeklyConstantSundayDuration,
            lblWeeklyConstantMondayDuration,
            lblWeeklyConstantTuesdayDuration,
            lblWeeklyConstantWednesdayDuration,
            lblWeeklyConstantThursdayDuration,
            lblWeeklyConstantFridayDuration,
            lblWeeklyConstantSundayDuration
        )
        lstTimes = listOf(
            btnWeeklyConstantSundayTime,
            btnWeeklyConstantMondayTime,
            btnWeeklyConstantTuesdayTime,
            btnWeeklyConstantWednesdayTime,
            btnWeeklyConstantThursdayTime,
            btnWeeklyConstantFridayTime,
            btnWeeklyConstantSaturdayTime
        )
        lstDurations = listOf(
            etxtWeeklyConstantSundayDuration,
            etxtWeeklyConstantMondayDuration,
            etxtWeeklyConstantTuesdayDuration,
            etxtWeeklyConstantWednesdayDuration,
            etxtWeeklyConstantThursdayDuration,
            etxtWeeklyConstantFridayDuration,
            etxtWeeklyConstantSaturdayDuration
        )

        databaseOperations = DatabaseOperations(this)
        userSettings = databaseOperations.getUserSettings()
        defaultDuration = userSettings[0].toString()
        radGrpAddEditClient.setOnCheckedChangeListener(this)
        // Each switch is set to follow similar rules. If it becomes checked, make the appropriate views visible and initialize the button text and
        // duration to the default. If it becomes unchecked, set views to invisible
        swWeeklyConstantIsMonday.setOnCheckedChangeListener { _, isChecked -> handleWeeklyConstSwitch(isChecked, 1)}
        swWeeklyConstantIsTuesday.setOnCheckedChangeListener { _, isChecked -> handleWeeklyConstSwitch(isChecked, 2) }
        swWeeklyConstantIsWednesday.setOnCheckedChangeListener { _, isChecked -> handleWeeklyConstSwitch(isChecked, 3) }
        swWeeklyConstantIsThursday.setOnCheckedChangeListener { _, isChecked -> handleWeeklyConstSwitch(isChecked, 4) }
        swWeeklyConstantIsFriday.setOnCheckedChangeListener { _, isChecked -> handleWeeklyConstSwitch(isChecked, 5) }
        swWeeklyConstantIsSaturday.setOnCheckedChangeListener { _, isChecked -> handleWeeklyConstSwitch(isChecked, 6) }
        swWeeklyConstantIsSunday.setOnCheckedChangeListener { _, isChecked -> handleWeeklyConstSwitch(isChecked, 0) }
        // Get client from passed client id
        intentClient = databaseOperations.getClient(intent.getIntExtra("id", 0))
        // If client is invalid/blank
        if (intentClient.id == 0){
            // Set flag
            isNew = true
            // Set title to add client
            setTitle(R.string.add_client_title)
        } else {
            // If valid client
            btnAddEditClientStartDate.text = intentClient.startDate
            btnAddEditClientEndDate.text = intentClient.endDate
            etxtAddEditClientName.setText(intentClient.name)
            // Populate layout depending upon client parameters
            when (intentClient.schedule.scheduleType) {
                ScheduleType.NO_SCHEDULE -> {
                    // Load default duration to field
                    etxtNoScheduleDuration.setText(intentClient.schedule.duration.toString())
                    radGrpAddEditClient.check(R.id.radIsNoSchedule)
                }
                // Use list of views to set appropriate view properties for client
                ScheduleType.WEEKLY_CONSTANT -> {
                    intentClient.schedule.daysList.forEachIndexed { index, it ->
                        if (it > 0) {
                            lstDays[index].isChecked = true
                            lstTimes[index].text = intentClient.getStrTime(index)
                            lstDurations[index].setText(intentClient.schedule.durationsList[index].toString())
                        }
                    }
                    radGrpAddEditClient.check(R.id.radIsWeeklyConst)
                }
                ScheduleType.WEEKLY_VARIABLE -> {
                    etxtWeeklyVariableNumSessions.setText(intentClient.schedule.days.toString())
                    etxtWeeklyVariableDuration.setText(intentClient.schedule.duration.toString())
                    radGrpAddEditClient.check(R.id.radIsWeeklyVar)
                }
                ScheduleType.MONTHLY_VARIABLE -> {
                    etxtMonthlyVariableNumSessions.setText(intentClient.schedule.days.toString())
                    etxtMonthlyVariableDuration.setText(intentClient.schedule.duration.toString())
                    radGrpAddEditClient.check(R.id.radIsMonthlyVar)
                }
                ScheduleType.BLANK -> finish()// Close activity is ScheduleType is invalid
            }
        }
    }

    private fun handleWeeklyConstSwitch(isChecked: Boolean, dayOfWeek: Int) {
        lstLabels[dayOfWeek].visibility = if (isChecked) View.VISIBLE else View.INVISIBLE
        lstTimes[dayOfWeek].visibility = if (isChecked) View.VISIBLE else View.INVISIBLE
        lstDurations[dayOfWeek].visibility = if (isChecked) View.VISIBLE else View.INVISIBLE
        if (isChecked){
            lstTimes[dayOfWeek].text = getString(R.string.time_format)
            lstDurations[dayOfWeek].setText(defaultDuration)
        }
    }

    override fun onCheckedChanged(p0: RadioGroup?, checkedID: Int) {
        val isNotNoSchedule = checkedID != R.id.radIsNoSchedule
        btnAddEditClientEndDate.visibility = if(isNotNoSchedule) View.VISIBLE else View.GONE
        lblAddEditClientEndDate.visibility = if(isNotNoSchedule) View.VISIBLE else View.GONE
        btnAddEditClientStartDate.visibility = if(isNotNoSchedule) View.VISIBLE else View.GONE
        lblAddEditClientStartDate.visibility = if(isNotNoSchedule) View.VISIBLE else View.GONE
        when (checkedID){
            //if not NoSchedule, make startDate and endDate views visible. if NoSchedule make views invisible
            //make the right include view visible
            R.id.radIsWeeklyConst -> {
                incAddEditClientWeeklyConstant.visibility = View.VISIBLE
                incAddEditClientWeeklyVariable.visibility = View.GONE
                incAddEditClientMonthlyVariable.visibility = View.GONE
                incAddEditClientNoSchedule.visibility = View.GONE
            }
            R.id.radIsWeeklyVar -> {
                etxtWeeklyVariableDuration.setText(defaultDuration)
                incAddEditClientWeeklyConstant.visibility = View.GONE
                incAddEditClientWeeklyVariable.visibility = View.VISIBLE
                incAddEditClientMonthlyVariable.visibility = View.GONE
                incAddEditClientNoSchedule.visibility = View.GONE
            }
            R.id.radIsMonthlyVar -> {
                etxtMonthlyVariableDuration.setText(defaultDuration)
                incAddEditClientWeeklyConstant.visibility = View.GONE
                incAddEditClientWeeklyVariable.visibility = View.GONE
                incAddEditClientMonthlyVariable.visibility = View.VISIBLE
                incAddEditClientNoSchedule.visibility = View.GONE
            }
            R.id.radIsNoSchedule -> {
                //reset values for when they become visible again and the value needs to be set again
                btnAddEditClientEndDate.text = getString(R.string.date_format)
                btnAddEditClientStartDate.text = getString(R.string.date_format)
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
        // First check, make sure the name does not contain any illegal characters or isBlank
        if (!StaticFunctions.badSQLText(name)) {
            // Create Client object depending upon the checked radio button
            val client: Client = when (radGrpAddEditClient.checkedRadioButtonId) {
                R.id.radIsWeeklyConst -> {
                    val startDate = btnAddEditClientStartDate.text.toString()
                    val endDate = btnAddEditClientEndDate.text.toString()
                    // Make sure startDate and endDate are not the default values
                    if (startDate == getString(R.string.date_format) || endDate == getString(R.string.date_format)){
                        Snackbar.make(view, "Error. Start date and/or end date was no chosen", Snackbar.LENGTH_LONG).show()
                        return
                    }
                    var hasDays = false
                    val days = mutableListOf(0,0,0,0,0,0,0)
                    val durations = mutableListOf(0,0,0,0,0,0,0)
                    // Loop through using the range of 0 -> length of lstDays (ie 0 -> 6)
                    lstDays.forEachIndexed { index, switch ->
                        if (switch.isChecked) {
                            hasDays = true
                            val time = lstTimes[index].text.toString()
                            // If time for day is not the default
                            if (time != getString(R.string.time_format))
                                // Add time as minute in day (format for database)
                                days[index] = StaticFunctions.getTimeInt(time)
                            // If time not set
                            else {
                                Snackbar.make(
                                    view,
                                    "Error. One or more of the times for a chosen day was not entered",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                return
                            }
                            val duration = lstDurations[index].text.toString()
                            // If the duration text is only digit (ie Int). Should always be true due to view's input type
                            if (duration.isDigitsOnly() && duration.isNotBlank()) {
                                // Check if duration fits within range of 0 <= duration <= 120. See Wiki
                                if (duration.toInt() in 1..120) {
                                    durations[index] = duration.toInt()
                                } else {
                                    Snackbar.make(
                                        view,
                                        "Error. The duration entered is 0 or greater than 120mins. See Wiki for more information",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    return
                                }
                            } else {
                                Snackbar.make(
                                    view,
                                    "Error. Duration is not an integer",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                return
                            }
                        }
                    }
                    if (!hasDays) {
                        Snackbar.make(
                            view,
                            "Error. No days selected",
                            Snackbar.LENGTH_LONG
                        ).show()
                        return
                    }
                    Client(
                        intentClient.id,
                        name,
                        Schedule(
                            ScheduleType.WEEKLY_CONSTANT,
                            days.sumBy { if(it > 0) 1 else 0 },
                            0,
                            days,
                            durations
                        ),
                        startDate,
                        endDate
                    )
                }
                R.id.radIsWeeklyVar -> {
                    val startDate = btnAddEditClientStartDate.text.toString()
                    val endDate = btnAddEditClientEndDate.text.toString()
                    val duration = etxtWeeklyVariableDuration.text.toString()
                    val sessions = etxtWeeklyVariableNumSessions.text.toString()
                    // Make sure startDate and endDate are not the default values
                    if (startDate == getString(R.string.date_format) || endDate == getString(R.string.date_format)) {
                        Snackbar.make(view, "Error. Start date and/or end date was no chosen", Snackbar.LENGTH_LONG).show()
                        return
                    }
                    // If the duration text is only digit (ie Int). Should always be true due to view's input type
                    if (duration.isDigitsOnly() && duration.isNotBlank()) {
                        // Check if duration fits within range of 0 <= duration <= 120. See Wiki
                        if (duration.toInt() > 120 || duration.toInt() <= 0) {
                            Snackbar.make(
                                view,
                                "Error. The duration entered is 0 or greater than 120mins. See Wiki for more information",
                                Snackbar.LENGTH_LONG
                            ).show()
                            return
                        }
                    } else {
                        Snackbar.make(
                            view,
                            "Error. Duration is not an integer",
                            Snackbar.LENGTH_LONG
                        ).show()
                        return
                    }
                    // If the # of sessions/week text is only digit (ie Int). Should always be true due to view's input type
                    if(sessions.isDigitsOnly() && sessions.isNotBlank()) {
                        // Check if # of sessions/week fits within range of 0 < # of sessions/week < 8
                        if (sessions.toInt() > 7 || sessions.toInt() <= 0) {
                            Snackbar.make(
                                view,
                                "Error. Number of sessions is too high or too low for a single week",
                                Snackbar.LENGTH_LONG
                            ).show()
                            return
                        }
                    } else {
                        Snackbar.make(
                            view,
                            "Error. Number of sessions is not an integer",
                            Snackbar.LENGTH_LONG
                        ).show()
                        return
                    }
                    // Construct Client object with collected data. Times is set to "0" as default
                    // If duration is set to 0 the default duration from the user settings is used
                    Client(
                        intentClient.id,
                        name,
                        Schedule(
                            ScheduleType.WEEKLY_VARIABLE,
                            sessions.toInt(),
                            if (duration.toInt() == 0) userSettings[0] else duration.toInt(),
                            mutableListOf(),
                            mutableListOf()
                        ),
                        startDate,
                        endDate
                    )
                }
                R.id.radIsMonthlyVar -> {
                    val startDate = btnAddEditClientStartDate.text.toString()
                    val endDate = btnAddEditClientEndDate.text.toString()
                    val duration = etxtMonthlyVariableDuration.text.toString()
                    val sessions = etxtMonthlyVariableNumSessions.text.toString()
                    // Make sure startDate and endDate are not the default values
                    if (startDate == getString(R.string.date_format) || endDate == getString(R.string.date_format)) {
                        Snackbar.make(
                            view,
                            "Error. Start date and/or end date was no chosen",
                            Snackbar.LENGTH_LONG
                        ).show()
                        return
                    }
                    // If the duration text is only digit (ie Int). Should always be true due to view's input type
                    if (duration.isDigitsOnly() && duration.isNotBlank()) {
                        // Check if duration fits within range of 0 <= duration <= 120. See Wiki
                        if (duration.toInt() > 120 || duration.toInt() <= 0) {
                            Snackbar.make(
                                view,
                                "Error. The duration entered is 0 or greater than 120mins. See Wiki for more information",
                                Snackbar.LENGTH_LONG
                            ).show()
                            return
                        }
                    } else {
                        Snackbar.make(
                            view,
                            "Error. Durations is not an integer",
                            Snackbar.LENGTH_LONG
                        ).show()
                        return
                    }
                    // If the # of sessions/month text is only digit (ie Int). Should always be true due to view's input type
                    if (sessions.isDigitsOnly() && sessions.isNotBlank()) {
                        // Check if # of sessions/week fits within range of 0 < # of sessions/week < 30
                        if (sessions.toInt() > 28 || sessions.toInt() <= 0) {
                            Snackbar.make(
                                view,
                                "Error. Number of sessions is too high or too low for a single month",
                                Snackbar.LENGTH_LONG
                            ).show()
                            return
                        }
                    } else {
                        Snackbar.make(
                            view,
                            "Error. Number of sessions is not an integer",
                            Snackbar.LENGTH_LONG
                        ).show()
                        return
                    }
                    // Construct Client object with collected data. Times is set to "0" as default
                    // If duration is set to 0 the default duration from the user settings is used
                    Client(
                        intentClient.id,
                        name,
                        Schedule(
                            ScheduleType.MONTHLY_VARIABLE,
                            sessions.toInt(),
                            if (duration.toInt() == 0) userSettings[0] else duration.toInt(),
                            mutableListOf(),
                            mutableListOf()
                        ),
                        startDate,
                        endDate
                    )
                }
                R.id.radIsNoSchedule -> {
                    val duration = etxtNoScheduleDuration.text.toString()
                    // If the duration text is only digit (ie Int). Should always be true due to view's input type
                    if (duration.isDigitsOnly() && duration.isNotBlank()) {
                        // Check if duration fits within range of 0 <= duration <= 120. See Wiki
                        if (duration.toInt() > 120 || duration.toInt() <= 0) {
                            Snackbar.make(
                                view,
                                "Error. The duration entered is 0 or greater than 120mins. See Wiki for more information",
                                Snackbar.LENGTH_LONG
                            ).show()
                            return
                        }
                    } else {
                        Snackbar.make(
                            view,
                            "Error. Duration is not an integer",
                            Snackbar.LENGTH_LONG
                        ).show()
                        return
                    }
                    // Construct Client object with collected data. Days, Times, StartDate and EndDate are set to "0" as default
                    // If duration is set to 0 the default duration from the user settings is used
                    Client(
                        intentClient.id,
                        name,
                        Schedule(
                            ScheduleType.NO_SCHEDULE,
                            0,
                            if (duration.toInt() == 0) userSettings[0] else duration.toInt(),
                            mutableListOf(),
                            mutableListOf()
                        ),
                        "0",
                        "0"
                    )
                }
                else -> {
                    // If somehow no radio button is selected a blank client is passed forward
                    Client.empty
                }
            }
            // If the blank client has moved forward, it will be ignored
            if (client.name != "") {
                // Get conflicts with other existing clients. String will be empty if no conflicts and filled with names if conflicts are found
                val conflicts = databaseOperations.checkClientConflict(client)
                // If no conflicts found
                if (conflicts.isEmpty()) {
                    if (isNew) {
                        if (databaseOperations.insertClient(client)) {
                            Snackbar.make(
                                view,
                                "Inserted new client",
                                Snackbar.LENGTH_LONG).show()
                            finish()
                        } else
                            Snackbar.make(
                                view,
                                "SQL Error inserting new client",
                                Snackbar.LENGTH_LONG).show()
                    } else {
                        if (databaseOperations.updateClient(client)) {
                            Snackbar.make(
                                view,
                                "Updated client",
                                Snackbar.LENGTH_LONG).show()
                            finish()
                        } else
                            Snackbar.make(
                                view,
                                "SQL Error updating client",
                                Snackbar.LENGTH_LONG).show()
                    }
                } else
                    Snackbar.make(
                        view,
                        "Error. Conflict with $conflicts",
                        Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(
                    view,
                    "Error. No radio button selected",
                    Snackbar.LENGTH_LONG).show()
            }
        } else {
            Snackbar.make(
                view,
                if (name.isBlank())
                    "Name is empty"
                else
                    "Invalid input character inside client name. See Wiki for more information",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}
