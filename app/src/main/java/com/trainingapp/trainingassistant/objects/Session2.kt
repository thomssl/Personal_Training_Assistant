package com.trainingapp.trainingassistant.objects

import com.trainingapp.trainingassistant.StaticFunctions
import java.util.*
import kotlin.collections.ArrayList

/**
 * Object to hold the assigned and collected data about a client's session
 * @param clientID id that corresponds to a client in the Clients table. Used to distinguish the owner of the session
 * @param clientName name that corresponds the id passed to the session. Found in Client table
 * @param dayTime date and time of the session as a String (later converted to a Calendar for better accessibility)
 * @param program Program object that contains the List of ExerciseSession objects that define the movements in a session
 * @param notes String of anything the user wants to remember about the session
 * @param duration duration of the session in minutes
 */
class Session2 (var clientID: Int, var clientName: String, dayTime: String, private var program: Program, var notes: String, var duration: Int): Comparable<Session2> {

    // Used to ask a Session object for a certain SQL command
    companion object {
        const val INSERT_COMMAND = 1
        const val UPDATE_COMMAND = 2
        const val DELETE_COMMAND = 3
    }

    var date: Calendar = StaticFunctions.getDate(dayTime)

    //used to validate that a session has exercises
    // stops the user from removing all exercises from a session than updating or confirming a session that has never had any exercises
    fun hasExercises(): Boolean = program.hasExercises()
    fun addExercise(exerciseSession: ExerciseSession) = program.addExercise(exerciseSession)
    fun updateExercise(exerciseSession: ExerciseSession, position: Int) = program.updateExercise(exerciseSession, position)
    fun removeExercise(exerciseSession: ExerciseSession) = program.removeExercise(exerciseSession)
    fun removeExercise(exercise: Exercise) = program.removeExercise(exercise)
    fun getExercise(exercise: Exercise): ExerciseSession = program.getExercise(exercise)
    fun getExercise(index: Int): ExerciseSession = program.getExercise(index)
    fun getExerciseCount(): Int = program.getExerciseCount()
    fun getProgramID(): Int = program.id
    fun updateProgramID(id: Int) { program.id = id }
    private fun getTime(): Int = (date[Calendar.HOUR_OF_DAY] * 60) + date[Calendar.MINUTE]//gets time as minutes in the day
    fun getTimeRange(): IntRange = getTime() until (getTime() + duration)//gets an IntRange from the start to end time

    /**
     * Method used to check new values for time, date or duration within a Session
     * The clone of the Session is exactly the same except for the values sent into the function
     * For example, a new date can be used in the clone to check conflicts with the new theoretical Session without altering the original Session
     * @return copy of the original Session with the added parameters substituting the original Sessions attributes
     */
    fun clone(dayTime: String = "", program: Program = Program(0, "", ArrayList()), notes: String = "", duration: Int = 0): Session2{
        return Session2(
            clientID,
            clientName,
            if (dayTime.isEmpty()) StaticFunctions.getStrDateTime(date) else dayTime,
            if (program.hasExercises()) this.program else program,
            if (notes.isEmpty()) this.notes else notes,
            if (duration == 0) this.duration else duration
        )
    }

    /**
     * Method to get a SQL command depending upon the desired command type passed as a companion object value
     * @param type value denotes a type of SQL command. See companion object for value possibilities
     * @param oldDayTime blank unless the session is being updated and the date or time is changing
     * @return SQL command requested as a String object
     */
    fun getSQLCommand(type: Int, oldDayTime: String = ""): String{
        val dayTime = StaticFunctions.getStrDateTime(date)
        if (type == 3)//if Delete command
            return "Delete From Session_log Where client_id = $clientID And datetime(dayTime) = datetime('$dayTime')"

        //return appropriate command. 1 = Insert, 2 = Update
        return when(type){
            1 -> "${program.getInsertProgramsCommand(clientID, dayTime)};Insert Into Session_log(client_id, dayTime, notes, duration) Values($clientID, '$dayTime', (Select program_id From Programs Where client_id = $clientID And datetime(dayTime) = " +
                    "datetime('$dayTime')), '$notes', $duration);${program.getInsertProgramExercisesCommand(clientID, dayTime)}"
            2 -> {
                "Update Session_log Set ${if (oldDayTime.isNotBlank())"dayTime = '$dayTime', " else ""}notes = '$notes', duration = $duration Where client_id = $clientID And datetime(dayTime) = " +
                        "datetime('${if (oldDayTime.isNotBlank()) oldDayTime else dayTime}');${program.getUpdateProgramsCommand(dayTime, oldDayTime)};${program.getUpdateProgramExercisesCommand()}"
            }
            else -> "error"
        }
    }

    override fun compareTo(other: Session2): Int {
        return this.getTime() - other.getTime()
    }
}