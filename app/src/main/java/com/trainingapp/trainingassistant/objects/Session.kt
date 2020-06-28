package com.trainingapp.trainingassistant.objects

import com.trainingapp.trainingassistant.StaticFunctions
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
class Session(
    val sessionID: Int,
    var clientID: Int,
    var clientName: String,
    dayTime: String,
    var notes: String,
    var duration: Int,
    var exercises: MutableList<ExerciseSession>
): Comparable<Session> {

    // Used to ask a Session object for a certain SQL command
    companion object {
        const val INSERT_COMMAND = 1
        const val UPDATE_COMMAND = 2
        const val DELETE_COMMAND = 3

        fun empty(id: Int, name: String, dayTime: String) = Session(
            0,
            id,
            name,
            dayTime,
            "",
            0,
            mutableListOf()
        )
    }

    var date: Calendar = StaticFunctions.getDate(dayTime)

    //used to validate that a session has exercises
    // stops the user from removing all exercises from a session than updating or confirming a session that has never had any exercises
    private fun hasExercises(): Boolean = exercises.size > 0
    fun addExercise(exerciseSession: ExerciseSession){
        exercises.add(exerciseSession)
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
    fun getExerciseCount(): Int = exercises.size

    //gets time as minutes in the day
    private fun getTime(): Int = (date[Calendar.HOUR_OF_DAY] * 60) + date[Calendar.MINUTE]
    //gets an IntRange from the start to end time
    fun getTimeRange(): IntRange = getTime() until (getTime() + duration)

    /**
     * Method used to check new values for time, date or duration within a Session
     * The clone of the Session is exactly the same except for the values sent into the function
     * For example, a new date can be used in the clone to check conflicts with the new theoretical Session without altering the original Session
     * @return copy of the original Session with the added parameters substituting the original Sessions attributes
     */
    fun clone(dayTime: String = "", exercises: MutableList<ExerciseSession> = mutableListOf(), notes: String = "", duration: Int = 0): Session{
        return Session(
            sessionID,
            clientID,
            clientName,
            if (dayTime.isEmpty()) StaticFunctions.getStrDateTime(date) else dayTime,
            if (notes.isEmpty()) this.notes else notes,
            if (duration == 0) this.duration else duration,
            if (exercises.size == 0) this.exercises else exercises
        )
    }
    private fun getAllExerciseInserts(): String{
        val strSessionID = if (sessionID == 0)
            "(Select session_id " +
            "From Session_Log " +
            "Where client_id = $clientID " +
            "And datetime(dayTime) = datetime('${StaticFunctions.getStrDateTime(date)}'))"
        else
            sessionID.toString()
        return exercises.joinToString { "($strSessionID, ${it.id}, '${it.sets}', '${it.reps}', '${it.resistance}', ${it.order})" }
    }
    private fun getDeleteSessionExercisesCommand() = "Delete From Session_Exercises Where session_id = $sessionID;"
    private fun getDeleteSessionLogCommand() = "Delete From Session_log Where session_id = $sessionID;"
    private fun getInsertExercisesCommand(): String {
        return if (hasExercises())
            "Insert Into Session_Exercises(session_id, exercise_id, sets, reps, resistance, exercise_order) Values ${getAllExerciseInserts()}"
        else ""
    }

    /**
     * Method to get a SQL commands depending upon the desired command type passed as a companion object value
     * @param type value denotes a type of SQL command. See companion object for value possibilities
     * @return List of SQL commands requested
     */
    fun getSQLCommands(type: Int): List<String> {
        val dayTime = StaticFunctions.getStrDateTime(date)
        //if Delete command
        if (type == 3)
            return listOf(getDeleteSessionLogCommand(), getDeleteSessionExercisesCommand())

        //return appropriate command. 1 = Insert, 2 = Update
        return when(type){
            1 -> listOf(
                    "Insert Into Session_log(client_id, dayTime, notes, duration) Values($clientID, '$dayTime', '$notes', $duration);",
                    getInsertExercisesCommand()
                )
            2 -> listOf(
                    "Update Session_log Set dayTime = '$dayTime', notes = '$notes', duration = $duration " +
                    "Where session_id = $sessionID;",
                    getDeleteSessionExercisesCommand(),
                    getInsertExercisesCommand()
                )
            else -> listOf("error")
        }
    }

    override fun compareTo(other: Session): Int {
        return this.getTime() - other.getTime()
    }
}