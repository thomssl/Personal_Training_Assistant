package com.trainingapp.personaltrainingassistant.objects

import com.trainingapp.personaltrainingassistant.StaticFunctions
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class Session(var clientID: Int, var clientName: String, dayTime: String, private var exercises: ArrayList<ExerciseSession>, var notes: String, var duration: Int): Comparable<Session> {

    var date: Calendar = StaticFunctions.getDate(dayTime)

    fun hasExercises(): Boolean = exercises.size > 0

    fun addExercise(exerciseSession: ExerciseSession){
        exercises.add(exerciseSession)
        exercises.sort()
    }

    fun updateExercise(exerciseSession: ExerciseSession, position: Int){
        exercises[position] = exerciseSession
        exercises.sort()
    }

    private fun removeExercise(position: Int){
        exercises.removeAt(position)
        exercises.sort()
    }

    fun removeExercise(exerciseSession: ExerciseSession){
        exercises.remove(exerciseSession)
        exercises.sort()
    }

    fun removeExercise(exercise: Exercise){
        var index = -1
        for (i in exercises.indices){
            if (exercise.id == exercises[i].id){
                index = i
                break
            }
        }
        removeExercise(index)
    }

    fun getExercise(exercise: Exercise): ExerciseSession {
        var index = -1
        for (i in exercises.indices){
            if (exercise.id == exercises[i].id){
                index = i
                break
            }
        }
        return getExercise(index)
    }
    fun getExercise(index: Int): ExerciseSession = exercises[index]
    fun getExerciseCount(): Int = exercises.size

    private fun getTime(): Int{
        return (date[Calendar.HOUR_OF_DAY] * 60) + date[Calendar.MINUTE]
    }

    fun getTimeRange(): IntRange = getTime() until (getTime() + duration)

    fun getSQLCommand(type: Int, oldDayTime: String = ""): String{
        if (type == 3)
            return "Delete From Session_log Where client_id = $clientID And datetime(dayTime) = datetime('${StaticFunctions.getStrDateTime(date)})'"
        val builderExercises = StringBuilder()
        val builderSets = StringBuilder()
        val builderReps = StringBuilder()
        val builderResistances = StringBuilder()
        val builderOrders = StringBuilder()

        for(exercise in exercises){
            builderExercises.append(exercise.id.toString() + ",")
            builderSets.append(exercise.sets + ",")
            builderReps.append(exercise.reps + ",")
            builderResistances.append(exercise.resistance + ",")
            builderOrders.append(exercise.order.toString() + ",")
        }
        if (builderExercises.isNotEmpty()){
            builderExercises.deleteCharAt(builderExercises.lastIndex)
            builderSets.deleteCharAt(builderSets.lastIndex)
            builderReps.deleteCharAt(builderReps.lastIndex)
            builderResistances.deleteCharAt(builderResistances.lastIndex)
            builderOrders.deleteCharAt(builderOrders.lastIndex)
        }

        return when(type){
            1 -> "Insert Into Session_log(client_id, dayTime, exercise_ids, sets, reps, resistances, exercise_order, notes, duration) Values($clientID, '${StaticFunctions.getStrDateTime(date)}', '$builderExercises', '$builderSets', '$builderReps', '$builderResistances', '$builderOrders', '$notes', $duration)"
            2 -> if (oldDayTime != "") "Update Session_log Set dayTime = '${StaticFunctions.getStrDateTime(date)}', exercise_ids = '$builderExercises', sets = '$builderSets', reps = '$builderReps', resistances = '$builderResistances', exercise_order = '$builderOrders', notes = '$notes', duration = $duration Where client_id = $clientID And datetime(dayTime) = datetime('$oldDayTime')" else "error"
            else -> "error"
        }
    }

    override fun compareTo(other: Session): Int {
        return this.getTime() - other.getTime()
    }
}