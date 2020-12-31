package com.trainingapp.trainingassistant.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import com.google.android.material.snackbar.Snackbar
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.StaticFunctions
import com.trainingapp.trainingassistant.database.DatabaseOperations
import com.trainingapp.trainingassistant.enumerators.ChangeStatus
import com.trainingapp.trainingassistant.enumerators.ScheduleType
import com.trainingapp.trainingassistant.objects.ExerciseSession
import com.trainingapp.trainingassistant.objects.Session
import com.trainingapp.trainingassistant.ui.adapters.SessionExercisesRVAdapter
import com.trainingapp.trainingassistant.ui.dialogs.AddExerciseSessionDialog
import com.trainingapp.trainingassistant.ui.dialogs.ChangeDurationDialog
import com.trainingapp.trainingassistant.ui.dialogs.EditExerciseSessionDialog
import kotlinx.android.synthetic.main.activity_session.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Activity to edit/insert sessions found in the user's schedule.
 * Session objects can be fully populated (session found in Session_log) or partially populated (session found in Session_Changes or from the
 * client's constant schedule)
 * Session's will be updated when 'Change Time', 'Change Duration', 'Change Date' or 'Confirm' Buttons are clicked (if the changes are valid)
 * Depending upon where the session is found, the session will be updated as follows:
 *      - in Session_log, entry will be updated
 *      - in Session_Change, if(any exercises are added to a blank session) insert record into Session_log, if(date, time or duration changes)
 *        update record in Session_Changes
 *      - in constant schedule, if(any exercises are added to a blank session) insert record into Session_log, if(date, time or duration changes)
 *        insert record into Session_Changes
 */
class SessionActivity : AppCompatActivity(), CoroutineScope, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    //used to hold the users intended new date & time when choosing a new date/time
    private val calendar = Calendar.getInstance()
    private var duration = 0
    private var changeDate = false
    private var changeTime = false
    private var changeDuration = false
    private var changeExercise = false
    private lateinit var databaseOperations: DatabaseOperations
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var timePickerDialog: TimePickerDialog
    private lateinit var session: Session
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)
        setTitle(R.string.session_activity)

        databaseOperations = DatabaseOperations(this)
        datePickerDialog = DatePickerDialog(
            this,
            R.style.DialogTheme,
            this,
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
        timePickerDialog = TimePickerDialog(
            this,
            R.style.DialogTheme,
            this,
            calendar[Calendar.HOUR_OF_DAY],
            calendar[Calendar.MINUTE],
            false
        )
    }

    /**
     * Method called when activity is resumed by the user. Overridden to also get values from the Intent passed to start activity.
     * Calls Coroutine function to load data passed
     */
    override fun onResume() {
        super.onResume()
        val sessionID = intent.getIntExtra("session_id", 0)
        val clientID = intent.getIntExtra("client_id", 0)
        val dayTime = intent.getStringExtra("dayTime")
        //checks to make sure the client id is valid
        if (clientID > 0)
            setupData(sessionID,clientID, dayTime)
    }

    override fun onBackPressed() {
        if (changeDate || changeDuration || changeTime || changeExercise){
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.alert_dialog_confirm_removal))
            alertDialog.setMessage(getString(R.string.confirm_unsaved_changes))
            alertDialog.setPositiveButton(R.string.yes_button) { _, _ ->
                if (!confirmSessionChanges())
                    Toast.makeText(this, "Session changes could not be successfully saved", Toast.LENGTH_LONG).show()
                else super.onBackPressed()
            }
            alertDialog.setNegativeButton(R.string.no_button) { dialog, _ ->
                dialog.dismiss()
                super.onBackPressed()
            }
            alertDialog.show()
        } else super.onBackPressed()
    }

    /**
     * Coroutine Method for the UI scope. Gets data to be presented to the user from a suspendable function. Sets UI will returned data
     * @param sessionID ID of the session (sessionID) or the session holder (clientID) passed through the Intent
     * @param dayTime dayTime of the session pass through the Intent, not included if the sessionID is valid (ie not 0)
     */
    private fun setupData(sessionID: Int, clientID: Int, dayTime: String?) = launch{
        if (dayTime.isNullOrBlank())
            finish()
        val result = getData(sessionID, clientID, dayTime!!)
        session = result
        calendar.time = session.time
        duration = session.duration
        datePickerDialog.updateDate(calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH])
        timePickerDialog.updateTime(calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE])
        txtSessionClientName.text = session.clientName
        setDate(ChangeStatus.NOTHING)
        setTime(ChangeStatus.NOTHING)
        setDuration(ChangeStatus.NOTHING)
        if (session.notes.isNotEmpty())
            etxtNotes.setText(session.notes)
        setAdapter()
    }

    /**
     * Suspendable Method to query database for session data
     */
    private suspend fun getData(sessionID: Int, clientID: Int, dayTime: String): Session = withContext(Dispatchers.IO){
        databaseOperations.getSession(clientID, dayTime, sessionID)
    }

    /**
     * Method to handle a time change request from the user. Checks to make sure a change has occurred.
     * Checks for any session conflicts then updates session
     * @param hour hour of the day selected by the user
     * @param minute minute of the hour selected by the user
     */
    override fun onTimeSet(p0: TimePicker?, hour: Int, minute: Int) {
        if (!(calendar[Calendar.HOUR_OF_DAY] == hour && calendar[Calendar.MINUTE] == minute)) {
            val tempCalendar = Calendar.getInstance()
            tempCalendar.time = calendar.time
            tempCalendar[Calendar.HOUR_OF_DAY] = hour
            tempCalendar[Calendar.MINUTE] = minute
            session.using(dayTime = StaticFunctions.getStrDateTime(tempCalendar.time)) {
                !databaseOperations.checkSessionConflict(it, true)
            }.run {
                if (this) {
                    calendar.time = tempCalendar.time
                    setTime(ChangeStatus.UNCONFIRMED)
                    changeTime = true
                }
                else
                    Snackbar.make(btnChangeTime, "Conflict with new time found. Please choose another time", Snackbar.LENGTH_LONG).show()
            }
/*            if (!databaseOperations.checkSessionConflict(session.clone(dayTime = StaticFunctions.getStrDateTime(tempCalendar.time)), true)) {
                calendar.time = tempCalendar.time
                setTime()
                changeTime = true
            }
            else
                Snackbar.make(btnChangeTime, "Conflict with new time found. Please choose another time", Snackbar.LENGTH_LONG).show()*/
        }
    }

    /**
     * Method to handle a date change request from the user. Checks to make sure a change has occurred.
     * Checks for any session conflicts then updates session
     * @param year year selected by the user
     * @param month month of the year selected by the user
     * @param dayOfMonth day of the month selected by the user
     */
    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        if (!(calendar[Calendar.YEAR] == year && calendar[Calendar.MONTH] == month && calendar[Calendar.DAY_OF_MONTH] == dayOfMonth)) {
            val tempCalendar = Calendar.getInstance()
            tempCalendar.time = calendar.time
            tempCalendar[Calendar.YEAR] = year
            tempCalendar[Calendar.MONTH] = month
            tempCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
            session.using(dayTime = StaticFunctions.getStrDateTime(tempCalendar.time)) {
                !databaseOperations.checkSessionConflict(it, false)
            }.run {
                if (this){
                    calendar.time = tempCalendar.time
                    setDate(ChangeStatus.UNCONFIRMED)
                    changeDate = true
                } else
                    Snackbar.make(btnChangeDate, "Conflict with new date found. Please choose another date", Snackbar.LENGTH_LONG).show()
            }
/*            if (!databaseOperations.checkSessionConflict(session.clone(dayTime = StaticFunctions.getStrDateTime(tempCalendar.time)), false)) {
                calendar.time = tempCalendar.time
                setDate()
                changeDate = true
            }
            else
                Snackbar.make(btnChangeDate, "Conflict with new date found. Please choose another date", Snackbar.LENGTH_LONG).show()*/
        }
    }

    //UI update functions
    private fun setDate(changeStatus: ChangeStatus) {
        txtSessionDate.text = StaticFunctions.getStrDate(calendar.time)
        txtSessionDate.setTextColor(Color.parseColor(changeStatus.rgb))
    }
    private fun setTime(changeStatus: ChangeStatus) {
        txtSessionTime.text = StaticFunctions.getStrTimeAMPM(calendar.time)
        txtSessionTime.setTextColor(Color.parseColor(changeStatus.rgb))
    }
    private fun setDuration(changeStatus: ChangeStatus) {
        txtSessionDuration.text = getString(R.string.txtSessionDuration, duration)
        txtSessionDuration.setTextColor(Color.parseColor(changeStatus.rgb))
    }
    private fun setAdapter(){
        rvSessionExercises.adapter = SessionExercisesRVAdapter(session, {
                exerciseSession, position -> onItemClick(exerciseSession, position)
        }, {
                exerciseSession -> onItemLongClick(exerciseSession)
        })
        rvSessionExercises.visibility = if (session.exerciseCount > 0) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Method to handle btnConfirmSession's onClick event. If the session has exercises attached, Update/Insert Session_log entry
     */
    fun clickBtnConfirmSession(view: View){
        if (confirmSessionChanges()) Snackbar.make(view, "Session updated", Snackbar.LENGTH_LONG).show()
        else Snackbar.make(view, "Error confirming changes", Snackbar.LENGTH_LONG).show()
    }

    /**
     * Method to handle updating session bases upon flags that show what aspects of the session have changed
     */
    private fun confirmSessionChanges(): Boolean{
        val result = when {
            // If the session date, duration or time has changed
            changeDate || changeDuration || changeTime -> {
                session.usingReturn(dayTime = StaticFunctions.getStrDateTime(calendar.time), duration = duration) {
                    if (databaseOperations.checkSessionLog(session)) {
                        if (!databaseOperations.updateSession(it))
                        // If updating returns an error flag, exit function with error flag
                            return@usingReturn false
                    }
                    else {
                        if (!databaseOperations.insertSession(it))
                        // If inserting returns an error flag, exit function with error flag
                            return@usingReturn false
                    }
                    when (true){
                        // If change exists in DB for old session, update the change
                        databaseOperations.checkChange(session) -> databaseOperations.updateChange(session, it)
                        // If client type is WEEKLY_CONSTANT but change does not exist, create the change record
                        databaseOperations.getClientType(session.clientID) == ScheduleType.WEEKLY_CONSTANT ->
                            databaseOperations.insertChange(session, it)
                        // No change record needed, set result to true
                        else -> true
                    }
                }
            }
            // If no date, duration or time changes happened, update the session record with the current Session object
            // ie only update the exercises information
            !changeDate && !changeDuration && !changeTime && changeExercise -> {
                // If Session exists update the session, if not insert a new session record into Session_Log
                if (databaseOperations.checkSessionLog(session)) Pair(databaseOperations.updateSession(session), Session.empty)
                else Pair(databaseOperations.insertSession(session), Session.empty)
            }
            // If no changed logged, do nothing
            else -> {
                Pair(false, Session.empty)
            }
        }
        // If updates/inserts were successful and the session date, duration or time was changed, update the current session object
        if (result.first && (result.second != Session.empty) && (changeDate || changeDuration || changeTime)) {
            /*session.time = calendar.time
            session.duration = duration*/
            session = result.second
        }
        // If update was successful, reset all the change flags
        result.first.let {
            var status = when(true){
                it && changeDate -> ChangeStatus.CONFIRMED
                !changeDate ->  ChangeStatus.NOTHING
                else -> ChangeStatus.ERROR
            }
            setDate(status)
            status = when(true){
                it && changeDuration -> ChangeStatus.CONFIRMED
                !changeDuration ->  ChangeStatus.NOTHING
                else -> ChangeStatus.ERROR
            }
            setDuration(status)
            status = when(true){
                it && changeTime -> ChangeStatus.CONFIRMED
                !changeTime ->  ChangeStatus.NOTHING
                else -> ChangeStatus.ERROR
            }
            setTime(status)
            //setDate(if (it) ChangeStatus.CONFIRMED else ChangeStatus.ERROR)
            if (it) {
                changeDate = false
                changeDuration = false
                changeTime = false
                changeExercise = false
            }
        }
        return result.first
    }

    fun clickBtnChangeDate(@Suppress("UNUSED_PARAMETER") view: View) {
        datePickerDialog.show()
    }

    fun clickBtnChangeTime(@Suppress("UNUSED_PARAMETER") view: View) {
        timePickerDialog.show()
    }

    fun clickBtnAddExerciseSession(@Suppress("UNUSED_PARAMETER") view: View){
        val addExerciseDialog = AddExerciseSessionDialog(session.clientID) {addExerciseSessionDialog ->
            onAddConfirmClick(addExerciseSessionDialog)
        }
        addExerciseDialog.show(supportFragmentManager, "Add Exercise")
    }

    fun clickBtnChangeDuration(@Suppress("UNUSED_PARAMETER") view: View){
        val changeDurationDialog = ChangeDurationDialog(session.duration) {duration ->
            onDurationChangeConfirm(duration)
        }
        changeDurationDialog.show(supportFragmentManager, "Change Duration")
    }

    /**
     * Method passed to ChangeDurationDialog to handle the output. Validates the new duration and uses that duration to update/insert the session.
     * Checks to make sure:
     *      - The duration from the EditText field is an Int (using a try catch block)
     *      - 0 mins < duration <= 120 mins
     *      - The new duration does not create a conflict with an existing session
     * If all tests are passed, the session is updated/inserted (update Session_log if record exists, update/insert if Session_Changes exists)
     * @param strDuration duration as a string from the dialog
     * @return true if input is valid and the session is updated without error, false if invalid data or error during update.
     * True will dismiss dialog, false will keep it visible
     */
    private fun onDurationChangeConfirm(strDuration: String): Boolean{
        return try{
            val tempDuration = strDuration.toInt()
            if (tempDuration > 120 || tempDuration <= 0){
                Toast.makeText(this, "Duration outside of acceptable values. See Wiki for more information", Toast.LENGTH_LONG).show()
                false
            } else {
                session.using (duration = tempDuration) {
                    databaseOperations.checkSessionConflict(it, true)
                }.let {
                    if (it) {
                        Toast.makeText(this, "Error: Session Conflict", Toast.LENGTH_LONG).show()
                        false
                    }
                    else {
                        changeDuration = true
                        duration = tempDuration
                        setDuration(ChangeStatus.UNCONFIRMED)
                        true
                    }
                }
/*                if (databaseOperations.checkSessionConflict(session.clone(duration = tempDuration), true)) {
                    Toast.makeText(this, "Error: Session Conflict", Toast.LENGTH_LONG).show()
                    false
                } else {
                    changeDuration = true
                    duration = tempDuration
                    setDuration()
                    true
                }*/
            }
        } catch (e: NumberFormatException){
            e.printStackTrace()
            Toast.makeText(this, "Duration is not an integer. Please enter valid input", Toast.LENGTH_LONG).show()
            false
        }
    }

    /**
     * Method passed to SessionExercisesRVAdapter to handle item onClick event. Allows the user to update an exercises attributes.
     * Does not allow the user to the change the Exercise itself
     * @param exerciseSession ExerciseSession object containing the current exercise attributes to be changed
     * @param position Index of the ExerciseSession within the Session object's ExerciseSession list that needs to be updated
     */
    private fun onItemClick(exerciseSession: ExerciseSession, position: Int){
        val editExerciseDialog = EditExerciseSessionDialog(exerciseSession, position) {editExerciseSessionDialog, i ->
            onEditConfirmClick(editExerciseSessionDialog,i)
        }
        editExerciseDialog.show(supportFragmentManager, "Edit Exercise")
    }

    /**
     * Method passed to SessionExercisesRVAdapter to handle item onLongClick event. Allows the user to remove an exercise from the ExerciseSession
     * list within the Session object
     * @param exerciseSession ExerciseSession object containing the current exercise attributes to be removed
     * @return always true since the callback consumed the long click (See Android View.onLongClickListener for more info)
     */
    private fun onItemLongClick(exerciseSession: ExerciseSession): Boolean{
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(getString(R.string.alert_dialog_confirm_removal))
        alertDialog.setMessage(getString(R.string.confirm_delete, exerciseSession.name))
        alertDialog.setPositiveButton(R.string.confirm) { _, _ -> session.removeExercise(exerciseSession); setAdapter()}
        alertDialog.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss()}
        alertDialog.show()
        return true
    }

    /**
     * Method passed to AddExerciseSessionDialog to handle confirm click or dialog output. Collects data from the dialog (user input) and validates.
     * If input is valid, add exercise to Session
     * @param sessionDialog Dialog seen by user with all data added by the user. Used to collect and validate input
     * @return true if input valid and the exercise was added to the session, false if input not valid. True will close the dialog, false will keep
     * it open so the user can fix the problem(s)
     */
    private fun onAddConfirmClick(sessionDialog: AddExerciseSessionDialog): Boolean {
        val dialogView: Dialog = sessionDialog.dialog!!
        //collect data input from user
        val exerciseName = dialogView.findViewById<AutoCompleteTextView>(R.id.actxtAddExerciseName).text.toString()
        val resistance = dialogView.findViewById<EditText>(R.id.etxtAddResistance).text.toString()
        val sets = dialogView.findViewById<EditText>(R.id.etxtAddSets).text.toString()
        val reps = dialogView.findViewById<EditText>(R.id.etxtAddReps).text.toString()
        val orderText = dialogView.findViewById<EditText>(R.id.etxtAddExerciseOrder).text
        val order: Int = if(orderText.isDigitsOnly() && orderText.isNotBlank())
            //if input in the exercise order field is digits (ie Int) and not blank convert the value to Int
            orderText.toString().toInt()
        else
            //if not an Int or Blank assign order to -1 (will not pass validation)
            -1
        when {
            order <= 0 -> Toast.makeText(this, "Order must be a number greater than 0", Toast.LENGTH_LONG).show()
            StaticFunctions.badSQLText(resistance) -> Toast.makeText(
                this,
                "Resistance contains a bad character or is blank. See Wiki for more details",
                Toast.LENGTH_LONG
            ).show()
            StaticFunctions.badSQLText(sets) -> Toast.makeText(
                this,
                "Sets contains a bad character or is blank. See Wiki for more details",
                Toast.LENGTH_LONG
            ).show()
            StaticFunctions.badSQLText(reps) -> Toast.makeText(
                this,
                "Reps contains a bad character or is blank. See Wiki for more details",
                Toast.LENGTH_LONG
            ).show()
            !sessionDialog.exerciseNames.contains(exerciseName) -> Toast.makeText(
                this,
                "No Exercise selected. Please choose from the list",
                Toast.LENGTH_LONG
            ).show()//make sure name is within those collected from the database
            else -> {//if the input passes all tests, get populate a new ExerciseSession object and add that object to the Session
                val exercise = sessionDialog.exercises[sessionDialog.exerciseNames.indexOf(exerciseName)]
                val exerciseSession = ExerciseSession(exercise, sets, reps, resistance, order)
                session.addExercise(exerciseSession)
                changeExercise = true
                setAdapter()
                return true
            }
        }
        return false
    }

    /**
     * Method passed to EditExerciseDialog to handle confirm click or dialog output. Collects data from the dialog (user input) and validates.
     * If input is valid, update exercise at the indicated position
     * @param sessionDialog Dialog seen by user with all data added by the user. Used to collect and validate input
     * @param position Index of the ExerciseSession within the Session object's ExerciseSession list that needs to be updated
     * @return true if input valid and the exercise was updated in the session, false if input not valid. True will close the dialog, false will keep
     * it open so the user can fix the problem(s)
     */
    private fun onEditConfirmClick(sessionDialog: EditExerciseSessionDialog, position: Int): Boolean {
        val dialogView: Dialog = sessionDialog.dialog!!
        //collect data input from user
        val resistance = dialogView.findViewById<EditText>(R.id.etxtEditResistance).text.toString()
        val sets = dialogView.findViewById<EditText>(R.id.etxtEditSets).text.toString()
        val reps = dialogView.findViewById<EditText>(R.id.etxtEditReps).text.toString()
        val orderText = dialogView.findViewById<EditText>(R.id.etxtEditExerciseOrder).text
        val order: Int = if(orderText.isDigitsOnly() && orderText.isNotBlank())
            //if input in the exercise order field is digits (ie Int) and not blank convert the value to Int
            orderText.toString().toInt()
        else
            //if not an Int or Blank assign order to -1 (will not pass validation)
            -1
        when (true){
            order <= 0 -> Toast.makeText(this, "Order must be a number greater than 0", Toast.LENGTH_LONG).show()
            StaticFunctions.badSQLText(resistance) -> Toast.makeText(
                this,
                "Resistance contains a bad character or is blank. See Wiki for more details",
                Toast.LENGTH_LONG
            ).show()
            StaticFunctions.badSQLText(sets) -> Toast.makeText(
                this,
                "Sets contains a bad character or is blank. See Wiki for more details",
                Toast.LENGTH_LONG
            ).show()
            StaticFunctions.badSQLText(reps) -> Toast.makeText(
                this,
                "Reps contains a bad character or is blank. See Wiki for more details",
                Toast.LENGTH_LONG
            ).show()
            else -> {//if the input passes all tests, get populate a new ExerciseSession object and add that object to the Session
                val exerciseSession = ExerciseSession(sessionDialog.exerciseSession.exercise, sets, reps, resistance, order)
                session.updateExercise(exerciseSession, position)
                changeExercise = true
                setAdapter()
                return true
            }
        }
        return false
    }
}
