package com.trainingapp.personaltrainingassistant.objects

import com.trainingapp.personaltrainingassistant.StaticFunctions
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

/**
 * Object to hold the assigned and collected data about a client's session
 * @param clientID id that corresponds to a client in the Clients table. Used to distinguish the owner of the session
 * @param clientName name that corresponds the id passed to the session. Found in Client table
 * @param dayTime date and time of the session as a String (later converted to a Calendar for better accessibility)
 * @param exercises List of ExerciseSession objects that define the movements in a session
 * @param notes String of anything the user wants to remember about the session
 * @param duration duration of the session in minutes
 */
class Session(var clientID: Int, var clientName: String, dayTime: String, private var exercises: ArrayList<ExerciseSession>, var notes: String, var duration: Int): Comparable<Session> {

    // Used to ask a Session object for a certain SQL command
    companion object {
        const val INSERT_COMMAND = 1
        const val UPDATE_COMMAND = 2
        const val DELETE_COMMAND = 3
    }

    var date: Calendar = StaticFunctions.getDate(dayTime)

    //used to validate that a session has exercises
    // stops the user from removing all exercises from a session than updating or confirming a session that has never had any exercises
    fun hasExercises(): Boolean = exercises.size > 0

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
    private fun getTime(): Int = (date[Calendar.HOUR_OF_DAY] * 60) + date[Calendar.MINUTE]//gets time as minutes in the day
    fun getTimeRange(): IntRange = getTime() until (getTime() + duration)//gets an IntRange from the start to end time

    /**
     * Method used to check new values for time, date or duration within a Session
     * The clone of the Session is exactly the same except for the values sent into the function
     * For example, a new date can be used in the clone to check conflicts with the new theoretical Session without altering the original Session
     * @return copy of the original Session with the added parameters substituting the original Sessions attributes
     */
    fun clone(dayTime: String = "", exercises: ArrayList<ExerciseSession> = ArrayList(), notes: String = "", duration: Int = 0): Session{
        return Session(
            clientID,
            clientName,
            if (dayTime.isEmpty()) StaticFunctions.getStrDateTime(date) else dayTime,
            if (exercises.size == 0) this.exercises else exercises,
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
        if (type == 3)//if Delete command
            return "Delete From Session_log Where client_id = $clientID And datetime(dayTime) = datetime('${StaticFunctions.getStrDateTime(date)})'"
        val builderExercises = StringBuilder()
        val builderSets = StringBuilder()
        val builderReps = StringBuilder()
        val builderResistances = StringBuilder()
        val builderOrders = StringBuilder()

        //build exercise ids, sets, reps, resistances and orders
        for(exercise in exercises){
            builderExercises.append(exercise.id.toString() + ",")
            builderSets.append(exercise.sets + ",")
            builderReps.append(exercise.reps + ",")
            builderResistances.append(exercise.resistance + ",")
            builderOrders.append(exercise.order.toString() + ",")
        }
        if (builderExercises.isNotEmpty()){//if session doesn't contains any exercises builderExercises will be empty. Avoid error
            builderExercises.deleteCharAt(builderExercises.lastIndex)
            builderSets.deleteCharAt(builderSets.lastIndex)
            builderReps.deleteCharAt(builderReps.lastIndex)
            builderResistances.deleteCharAt(builderResistances.lastIndex)
            builderOrders.deleteCharAt(builderOrders.lastIndex)
        }

        //return appropriate command. 1 = Insert, 2 = Update
        return when(type){
            1 -> "Insert Into Session_log(client_id, dayTime, exercise_ids, sets, reps, resistances, exercise_order, notes, duration) Values($clientID, '${StaticFunctions.getStrDateTime(date)}', '$builderExercises', '$builderSets', '$builderReps', '$builderResistances', '$builderOrders', '$notes', $duration)"
            2 -> {
                if (oldDayTime != "")
                    "Update Session_log Set dayTime = '${StaticFunctions.getStrDateTime(date)}', exercise_ids = '$builderExercises', sets = '$builderSets', reps = '$builderReps', resistances = '$builderResistances', exercise_order = '$builderOrders', notes = '$notes', duration = $duration Where client_id = $clientID And datetime(dayTime) = datetime('$oldDayTime')"
                else
                    "Update Session_log Set exercise_ids = '$builderExercises', sets = '$builderSets', reps = '$builderReps', resistances = '$builderResistances', exercise_order = '$builderOrders', notes = '$notes', duration = $duration Where client_id = $clientID And datetime(dayTime) = datetime('${StaticFunctions.getStrDateTime(date)}')"
            }
            else -> "error"
        }
    }

    override fun compareTo(other: Session): Int {
        return this.getTime() - other.getTime()
    }
}