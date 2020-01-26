package com.trainingapp.trainingassistant.objects

import com.trainingapp.trainingassistant.enumerators.ExerciseType
import java.lang.StringBuilder

/**
 * Object to hold gathered data about an exercise (abstract exercise definition, not sets, reps or resistance)
 * @param id Unique key given to the exercise when a new exercise is inserted. Used to differentiate exercises within sessions
 * @param name Unique string of text given as the exercise's name. User submitted but must be unique to differentiate exercises. User can use any form of differentiation they desire
 * @param type ExerciseType enum value given to the exercise. Used to define what the primary and secondary movers are (Muscles or Joints)
 * @param primaryMover MuscleJoint object containing the name and id to the primary mover
 * @param secondaryMovers List of the MuscleJoint objects that contain the names and ids of the secondary movers
 */
class Exercise2(val id: Int, var name: String, var type: ExerciseType, var primaryMover: MuscleJoint, private val secondaryMovers: ArrayList<MuscleJoint>) {

    /**
     * Method to get the names of all the secondary movers as a joined string
     */
    fun getSecondaryMoversNames(): String{
        val builder = StringBuilder()
        if (secondaryMovers.size > 0) {
            secondaryMovers.forEach { builder.append(it.name);builder.append("\n") }
            builder.deleteCharAt(builder.lastIndex)
        }
        return builder.toString()
    }

    /**
     * Private method to get the ids of all the secondary movers as a joined string. Used for database operations
     */
    private fun getDatabaseSecondaryMovers(): String{
        return if (secondaryMovers.size > 0) {
            val builder = StringBuilder()
            builder.append(" Insert Into Secondary_Movers(exercise_id, secondary_mover_id) Values ")
            secondaryMovers.forEach { builder.append("($id,${it.id}),") }
            builder.deleteCharAt(builder.lastIndex)
            builder.toString()
        } else ""
    }

    /**
     * Method to get a copy of the secondary movers list
     */
    fun getLstSecondaryMovers(): ArrayList<MuscleJoint> = ArrayList(secondaryMovers.toList())

    //secondary movers operations
    fun addSecondaryMover(muscleJoint: MuscleJoint) = secondaryMovers.add(muscleJoint)
    fun removeSecondaryMover(muscleJoint: MuscleJoint) = secondaryMovers.remove(muscleJoint)
    fun clearSecondaryMovers() = secondaryMovers.clear()

    //database operations
    fun getInsertCommand(): String = "Insert Into Exercises(exercise_name, exercise_type, primary_mover) Values('$name', ${type.num}, ${primaryMover.id});${getDatabaseSecondaryMovers()}"
    fun getUpdateCommand(): String = "Update Exercises Set exercise_name = '$name', exercise_type = ${type.num}, primary_mover = ${primaryMover.id} Where exercise_id = $id; Delete From Secondary_Movers Where exercise_id = $id;" +
        getDatabaseSecondaryMovers()
    fun getDeleteCommand(): String = "Delete From Exercises Where exercise_id = $id;Delete From Secondary_Movers Where exercise_id = $id"
}