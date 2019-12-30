package com.trainingapp.personaltrainingassistant.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.text.isDigitsOnly
import com.google.android.material.snackbar.Snackbar
import com.trainingapp.personaltrainingassistant.database.DatabaseOperations
import com.trainingapp.personaltrainingassistant.R
import com.trainingapp.personaltrainingassistant.StaticFunctions
import com.trainingapp.personaltrainingassistant.ui.adapters.SessionExercisesRVAdapter
import com.trainingapp.personaltrainingassistant.ui.dialogs.AddExerciseSessionDialog
import com.trainingapp.personaltrainingassistant.ui.dialogs.EditExerciseSessionDialog
import com.trainingapp.personaltrainingassistant.objects.ExerciseSession
import com.trainingapp.personaltrainingassistant.objects.Session
import com.trainingapp.personaltrainingassistant.ui.dialogs.ChangeDurationDialog
import kotlinx.android.synthetic.main.activity_session.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.NumberFormatException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

class SessionActivity : AppCompatActivity(), CoroutineScope, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private val calendar = Calendar.getInstance()
    private lateinit var databaseOperations: DatabaseOperations
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var timePickerDialog: TimePickerDialog
    private lateinit var session: Session
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)
        setTitle(R.string.session_activity)

        databaseOperations = DatabaseOperations(this)
        datePickerDialog = DatePickerDialog(this, R.style.DialogTheme, this, calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH])
        timePickerDialog = TimePickerDialog(this, R.style.DialogTheme, this, calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE], false)
    }

    override fun onResume() {
        super.onResume()
        val id = intent.getIntExtra("client_id", 0)
        val dayTime = intent.getStringExtra("dayTime")
        setupData(id, dayTime)
    }

    private fun setupData(clientID: Int, dayTime: String) = launch{
        val result = getData(clientID,dayTime)
        session = result
        calendar.time = session.date.time
        datePickerDialog.updateDate(calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH])
        timePickerDialog.updateTime(calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE])
        txtSessionClientName.text = session.clientName
        setDate()
        setTime()
        Log.d("here", session.duration.toString())
        txtSessionDuration.text = getString(R.string.txtSessionDuration, session.duration)
        if (session.notes.isNotEmpty())
            etxtNotes.setText(session.notes)
        setAdapter()
    }

    private suspend fun getData(clientID: Int, dayTime: String): Session = withContext(Dispatchers.IO){
        databaseOperations.getSession(clientID, dayTime)
    }

    override fun onTimeSet(p0: TimePicker?, hour: Int, minute: Int) {
        if (!(calendar[Calendar.HOUR_OF_DAY] == hour && calendar[Calendar.MINUTE] == minute)) {
            session.date[Calendar.HOUR_OF_DAY] = hour
            session.date[Calendar.MINUTE] = minute
            if (!databaseOperations.checkSessionConflict(session, true)) {
                if (updateSession())  {
                    setTime()
                    calendar.time = session.date.time
                    Snackbar.make(btnChangeTime, "Updated Session Time", Snackbar.LENGTH_LONG).show()
                } else
                    Snackbar.make(btnChangeTime, "Error updating session information", Snackbar.LENGTH_LONG).show()
            }
            else {
                Snackbar.make(btnChangeTime, "Conflict with new time found. Please choose another time", Snackbar.LENGTH_LONG).show()
                session.date.time = calendar.time
            }
        }
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        if (!(calendar[Calendar.YEAR] == year && calendar[Calendar.MONTH] == month && calendar[Calendar.DAY_OF_MONTH] == dayOfMonth)) {
            session.date[Calendar.YEAR] = year
            session.date[Calendar.MONTH] = month
            session.date[Calendar.DAY_OF_MONTH] = dayOfMonth
            if (!databaseOperations.checkSessionConflict(session, false)) {
                if (updateSession())  {
                    setDate()
                    calendar.time = session.date.time
                    Snackbar.make(btnChangeDate, "Updated Session Date", Snackbar.LENGTH_LONG).show()
                } else
                    Snackbar.make(btnChangeDate, "Error updating session information", Snackbar.LENGTH_LONG).show()
            }
            else {
                Snackbar.make(btnChangeDate, "Conflict with new date found. Please choose another date", Snackbar.LENGTH_LONG).show()
                session.date.time = calendar.time
            }
        }
    }

    private fun setDate() { txtSessionDate.text = StaticFunctions.getStrDate(session.date) }
    private fun setTime() { txtSessionTime.text = StaticFunctions.getStrTimeAMPM(session.date) }

    private fun setAdapter(){
        if (session.getExerciseCount() > 0){
            rvSessionExercises.adapter = SessionExercisesRVAdapter(session, { exerciseSession, position -> onItemClick(exerciseSession, position) }, { exerciseSession -> onItemLongClick(exerciseSession) })
            rvSessionExercises.visibility = View.VISIBLE
        }  else {
            rvSessionExercises.adapter = SessionExercisesRVAdapter(session, { exerciseSession, position -> onItemClick(exerciseSession, position) }, { exerciseSession -> onItemLongClick(exerciseSession) })
            rvSessionExercises.visibility = View.INVISIBLE
        }
    }

    private fun updateSession(): Boolean{
        val oldSession = Session(session.clientID, session.clientName, StaticFunctions.getStrDateTime(calendar), ArrayList(), "", session.duration)
        Snackbar.make(btnChangeDate, "Session updated", Snackbar.LENGTH_LONG).show()
        return when (true){
            databaseOperations.checkSessionLog(oldSession) -> databaseOperations.updateSession(session, StaticFunctions.getStrDateTime(calendar))
            databaseOperations.checkChange(oldSession) -> databaseOperations.updateChange(oldSession, session)
            else -> databaseOperations.insertChange(oldSession, session)
        }
    }

    fun clickBtnConfirmSession(view: View){
        if (session.hasExercises()){
            if (databaseOperations.checkSessionLog(session)) databaseOperations.updateSession(session, "")
            else databaseOperations.insertSession(session)
            Snackbar.make(view, "Session exercises inserted", Snackbar.LENGTH_LONG).show()
        } else {
            Snackbar.make(view, "No Exercises added. Add exercises to confirm session exercise changes", Snackbar.LENGTH_LONG).show()
        }
    }

    fun clickBtnChangeDate(view: View){
        datePickerDialog.show()
    }

    fun clickBtnChangeTime( view: View){
        timePickerDialog.show()
    }

    fun clickBtnAddExerciseSession(view: View){
        val addExerciseDialog = AddExerciseSessionDialog(session.clientID) {addExerciseSessionDialog -> onAddConfirmClick(addExerciseSessionDialog, view) }
        addExerciseDialog.show(supportFragmentManager, "Add Exercise")
    }

    fun clickBtnChangeDuration(view: View){
        val changeDurationDialog = ChangeDurationDialog(session.duration) {duration -> onDurationChangeConfirm(duration, view)}
        changeDurationDialog.show(supportFragmentManager, "Change Duration")
    }

    private fun onDurationChangeConfirm(strDuration: String, view: View): Boolean{
        return try{
            val duration = strDuration.toInt()
            if (duration > 120 || duration <= 0){
                Snackbar.make(view, "Duration outside of acceptable values. See Wiki for more information", Snackbar.LENGTH_LONG).show()
                false
            } else {
                session.duration = duration
                true
            }
        } catch (e: NumberFormatException){
            e.printStackTrace()
            Snackbar.make(view, "Duration is not an integer. Please enter valid input", Snackbar.LENGTH_LONG).show()
            false
        }
    }

    private fun onItemClick(exerciseSession: ExerciseSession, position: Int){
        val editExerciseDialog = EditExerciseSessionDialog(exerciseSession, position) {editExerciseSessionDialog, i -> onEditConfirmClick(editExerciseSessionDialog,i) }
        editExerciseDialog.show(supportFragmentManager, "Edit Exercise")
    }

    private fun onItemLongClick(exerciseSession: ExerciseSession): Boolean{
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(getString(R.string.confirm_delete, exerciseSession.name))
        alertDialog.setPositiveButton(R.string.confirm) { _, _ -> session.removeExercise(exerciseSession); setAdapter()}
        alertDialog.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss()}
        alertDialog.show()
        return true
    }

    private fun onAddConfirmClick(sessionDialog: AddExerciseSessionDialog, view: View): Boolean {
        val dialogView = sessionDialog.dialog
        val exerciseName = dialogView.findViewById<AutoCompleteTextView>(R.id.actxtAddExerciseName).text.toString()
        val resistance = dialogView.findViewById<EditText>(R.id.etxtAddResistance).text.toString()
        val sets = dialogView.findViewById<EditText>(R.id.etxtAddSets).text.toString()
        val reps = dialogView.findViewById<EditText>(R.id.etxtAddReps).text.toString()
        val order: Int = if(dialogView.findViewById<EditText>(R.id.etxtAddExerciseOrder).text.isDigitsOnly() && dialogView.findViewById<EditText>(R.id.etxtAddExerciseOrder).text.toString().isNotEmpty())
            dialogView.findViewById<EditText>(R.id.etxtAddExerciseOrder).text.toString().toInt()
        else
            -1
        when (true){
            order <= 0 -> Snackbar.make(view, "Order must be a number greater than 0", Snackbar.LENGTH_LONG).show()
            resistance.isEmpty() -> Snackbar.make(view, "Resistance is empty", Snackbar.LENGTH_LONG).show()
            sets.isEmpty() -> Snackbar.make(view, "sets is empty", Snackbar.LENGTH_LONG).show()
            reps.isEmpty() -> Snackbar.make(view, "reps is empty", Snackbar.LENGTH_LONG).show()
            StaticFunctions.badSQLText(resistance) -> Snackbar.make(view, "Resistance contains a bad character. See Wiki for more details", Snackbar.LENGTH_LONG).show()
            StaticFunctions.badSQLText(sets) -> Snackbar.make(view, "Sets contains a bad character. See Wiki for more details", Snackbar.LENGTH_LONG).show()
            StaticFunctions.badSQLText(reps) -> Snackbar.make(view, "Reps contains a bad character. See Wiki for more details", Snackbar.LENGTH_LONG).show()
            !sessionDialog.exerciseNames.contains(exerciseName) -> Snackbar.make(view, "No Exercise selected. Please choose from the list", Snackbar.LENGTH_LONG).show()
            else -> {
                val exercise = sessionDialog.exercises[sessionDialog.exerciseNames.indexOf(exerciseName)]
                val exerciseSession = ExerciseSession(exercise, StaticFunctions.formatForSQL(sets), StaticFunctions.formatForSQL(reps), StaticFunctions.formatForSQL(resistance), order)
                session.addExercise(exerciseSession)
                setAdapter()
                return true
            }
        }
        return false
    }

    private fun onEditConfirmClick(sessionDialog: EditExerciseSessionDialog, position: Int): Boolean {
        val dialogView = sessionDialog.dialog
        val resistance = dialogView.findViewById<EditText>(R.id.etxtEditResistance).text.toString()
        val sets = dialogView.findViewById<EditText>(R.id.etxtEditSets).text.toString()
        val reps = dialogView.findViewById<EditText>(R.id.etxtEditReps).text.toString()
        val order: Int = if(dialogView.findViewById<EditText>(R.id.etxtEditExerciseOrder).text.isDigitsOnly() && dialogView.findViewById<EditText>(R.id.etxtEditExerciseOrder).text.toString().isNotEmpty())
            dialogView.findViewById<EditText>(R.id.etxtEditExerciseOrder).text.toString().toInt()
        else
            -1
        when (true){
            order <= 0 -> Snackbar.make(rvSessionExercises, "Order must be a number greater than 0", Snackbar.LENGTH_LONG).show()
            resistance.isEmpty() -> Snackbar.make(rvSessionExercises, "Resistance is empty", Snackbar.LENGTH_LONG).show()
            sets.isEmpty() -> Snackbar.make(rvSessionExercises, "sets is empty", Snackbar.LENGTH_LONG).show()
            reps.isEmpty() -> Snackbar.make(rvSessionExercises, "reps is empty", Snackbar.LENGTH_LONG).show()
            StaticFunctions.badSQLText(resistance) -> Snackbar.make(rvSessionExercises, "Resistance contains a bad character. See Wiki for more details", Snackbar.LENGTH_LONG).show()
            StaticFunctions.badSQLText(sets) -> Snackbar.make(rvSessionExercises, "Sets contains a bad character. See Wiki for more details", Snackbar.LENGTH_LONG).show()
            StaticFunctions.badSQLText(reps) -> Snackbar.make(rvSessionExercises, "Reps contains a bad character. See Wiki for more details", Snackbar.LENGTH_LONG).show()
            else -> {
                val exerciseSession = ExerciseSession(sessionDialog.exerciseSession.getExercise(), StaticFunctions.formatForSQL(sets), StaticFunctions.formatForSQL(reps), StaticFunctions.formatForSQL(resistance), order)
                session.updateExercise(exerciseSession, position)
                setAdapter()
                return true
            }
        }
        return false
    }
}
