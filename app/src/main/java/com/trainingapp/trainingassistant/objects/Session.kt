package com.trainingapp.trainingassistant.objects

import android.database.Cursor
import com.trainingapp.trainingassistant.StaticFunctions
import com.trainingapp.trainingassistant.database.DBInfo
import java.util.*

/**
 * Object to hold the assigned and collected data about a client's session
 * @param clientID id that corresponds to a client in the Clients table. Used to distinguish the owner of the session
 * @param clientName name that corresponds the id passed to the session. Found in Client table
 * @param dayTime date and time of the session as a String (later converted to a Calendar for better accessibility)
 * @param notes String of anything the user wants to remember about the session
 * @param duration duration of the session in minutes
 * @param exercises list of exercises performed during the session
 */
class Session (
    val sessionID: Int,
    val clientID: Int,
    val clientName: String,
    private var dayTime: String,
    val notes: String,
    val duration: Int,
    val exercises: MutableList<ExerciseSession>
): Comparable<Session> {

    // Used to ask a Session object for a certain SQL command
    companion object {
        fun empty(id: Int, name: String, dayTime: String) = Session(
            0,
            id,
            name,
            dayTime,
            "",
            0,
            mutableListOf()
        )

        val empty = Session(
            0,
            0,
            "",
            "1970-01-01",
            "",
            0,
            mutableListOf()
        )

        fun withCursor(it: Cursor, sessionID: Int = -1, dayTime: String? = null, notes: String? = null, duration: Int = 0): Session{
            return Session(
                if (sessionID == -1) it.getInt(it.getColumnIndex(DBInfo.SessionLogTable.SESSION_ID)) else sessionID,
                it.getInt(it.getColumnIndex(DBInfo.SessionLogTable.CLIENT_ID)),
                it.getString(it.getColumnIndex(DBInfo.ClientsTable.NAME)),
                dayTime ?: it.getString(it.getColumnIndex(DBInfo.SessionLogTable.DAYTIME)),
                notes ?: it.getString(it.getColumnIndex(DBInfo.SessionLogTable.NOTES)),
                if (duration == 0) it.getInt(it.getColumnIndex(DBInfo.SessionLogTable.DURATION)) else duration,
                mutableListOf()
            )
        }
    }

    private val sessionDate: Calendar = StaticFunctions.getDate(dayTime)
    var time: Date
        get() = sessionDate.time
        set(value) {
            sessionDate.time = value
            dayTime = StaticFunctions.getStrDateTime(time)
        }
    val strDayTime: String
        get() = dayTime

    // Used to validate that a session has exercises
    // Stops the user from removing all exercises from a session than updating or confirming a session that has never had any exercises
    private val hasExercises: Boolean
        get() = exercises.size > 0
    fun addExercise(exerciseSession: ExerciseSession){
        exercises.add(exerciseSession)
        exercises.sort()
    }

    fun addExercises(exerciseSessions: List<ExerciseSession>){
        exercises.addAll(exerciseSessions)
        exercises.sort()
    }

    fun addExercises(exerciseSessions: Sequence<ExerciseSession>){
        exercises.addAll(exerciseSessions)
        exercises.sort()
    }

    fun updateExercise(exerciseSession: ExerciseSession, position: Int){
        exercises[position] = exerciseSession
        exercises.sort()
    }

    fun removeExercise(exerciseSession: ExerciseSession){
        exercises.remove(exerciseSession)
        exercises.sort()
    }

    /**
     * Method to remove an ExerciseSession based upon a passed Exercise object
     * Finds index of the ExerciseSession that contains the Exercise object passed
     * if the index is valid (index > -1), remove the ExerciseSession at that index and sort the remaining sessions
     * if the index is invalid, do nothing
     */
    fun removeExercise(exercise: Exercise){
        var index = -1
        for (i in exercises.indices){
            if (exercise.id == exercises[i].id){
                index = i
                break
            }
        }
        if (index >= 0) {
            exercises.removeAt(index)
            exercises.sort()
        }
    }

    /**
     * Method to get an ExerciseSession based upon a passed Exercise object
     * Finds the index of the ExerciseSession that contains the Exercise object passed
     * if the index is valid (index > -1), return the ExerciseSession at that index
     * if the index is invalid, return an empty ExerciseSession
     */
    fun getExercise(exercise: Exercise): ExerciseSession {
        var index = -1
        for (i in exercises.indices){
            if (exercise.id == exercises[i].id){
                index = i
                break
            }
        }
        return if (index > -1) getExercise(index) else ExerciseSession(exercise, "", "", "", 0)
    }

    fun getExercise(index: Int): ExerciseSession = exercises[index]
    val exerciseCount: Int
        get() = exercises.size

    //gets time as minutes in the day
    private val timeInt: Int
        get() = (sessionDate[Calendar.HOUR_OF_DAY] * 60) + sessionDate[Calendar.MINUTE]

    //gets an IntRange from the start to end time
    val timeRange: IntRange
        get() = timeInt until (timeInt + duration)

    /**
     * Method used to check new values for time, date or duration within a Session
     * The clone of the Session is exactly the same except for the values sent into the function
     * For example, a new date can be used in the clone to check conflicts with the new theoretical Session without altering the original Session
     * @return copy of the original Session with the added parameters substituting the original Sessions attributes
     */
    fun clone(
        dayTime: String = "",
        exercises: MutableList<ExerciseSession> = mutableListOf(),
        notes: String = "",
        duration: Int = 0
    ): Session{
        return Session(
            sessionID,
            clientID,
            clientName,
            if (dayTime.isEmpty()) StaticFunctions.getStrDateTime(time) else dayTime,
            if (notes.isEmpty()) this.notes else notes,
            if (duration == 0) this.duration else duration,
            if (exercises.size == 0) this.exercises else exercises
        )
    }

    fun using (
        dayTime: String = "",
        exercises: MutableList<ExerciseSession> = mutableListOf(),
        notes: String = "",
        duration: Int = 0,
        func: (session: Session) -> Boolean
    ): Boolean {
        val newSession = clone(dayTime, exercises, notes, duration)
        return func(newSession)
    }

    fun usingReturn (
        dayTime: String = "",
        exercises: MutableList<ExerciseSession> = mutableListOf(),
        notes: String = "",
        duration: Int = 0,
        func: (session: Session) -> Boolean
    ): Pair<Boolean,Session> {
        val newSession = clone(dayTime, exercises, notes, duration)
        val result = func(newSession)
        return Pair(result, if (result) newSession else this)
    }

    private val allExerciseInserts: String
        get() {
            val strSessionID = if (sessionID == 0)
                "(Select session_id " +
                        "From Session_Log " +
                        "Where client_id = $clientID " +
                        "And datetime(dayTime) = datetime('${StaticFunctions.getStrDateTime(time)}'))"
            else
                sessionID.toString()
            return exercises.joinToString { "($strSessionID, ${it.id}, '${it.sets}', '${it.reps}', '${it.resistance}', ${it.order})" }
        }
    private val deleteSessionExercisesCommand get() = "Delete From Session_Exercises Where session_id = $sessionID;"
    private val deleteSessionLogCommand get() = "Delete From Session_log Where session_id = $sessionID;"
    private val insertExercisesCommand: String
        get() {
            return if (hasExercises)
                "Insert Into Session_Exercises(session_id, exercise_id, sets, reps, resistance, exercise_order) Values $allExerciseInserts"
            else ""
        }

    val deleteCommands
        get() = listOf(deleteSessionLogCommand, deleteSessionExercisesCommand)
    val insertCommands
        get() = listOf(
            "Insert Into Session_log(client_id, dayTime, notes, duration) Values($clientID, '$dayTime', '$notes', $duration);",
            insertExercisesCommand
        )
    val updateCommands
        get() = listOf(
            "Update Session_log Set dayTime = '$dayTime', notes = '$notes', duration = $duration " +
                    "Where session_id = $sessionID;",
            deleteSessionExercisesCommand,
            insertExercisesCommand
        )

    override fun compareTo(other: Session): Int {
        return this.timeInt - other.timeInt
    }
}