package com.trainingapp.trainingassistant.objects

import android.database.Cursor
import com.trainingapp.trainingassistant.StaticFunctions
import com.trainingapp.trainingassistant.database.DBInfo
import com.trainingapp.trainingassistant.enumerators.ExerciseType

/**
 * Object to hold gathered data about an exercise (abstract exercise definition, not sets, reps or resistance)
 * @param id Unique key given to the exercise when a new exercise is inserted. Used to differentiate exercises within sessions
 * @param name Unique string of text given as the exercise's name. User submitted but must be unique to differentiate exercises.
 *             User can use any form of differentiation they desire
 * @param type ExerciseType enum value given to the exercise. Used to define what the primary and secondary movers are (Muscles or Joints)
 * @param primaryMover MuscleJoint object containing the name and id to the primary mover
 * @param secondaryMovers List of the MuscleJoint objects that contain the names and ids of the secondary movers
 */
class Exercise(
    val id: Int,
    var name: String,
    var type: ExerciseType,
    var primaryMover: MuscleJoint,
    private val secondaryMovers: MutableList<MuscleJoint>
) {

    companion object {
        val empty = Exercise(
            0,
            "",
            ExerciseType.BLANK,
            MuscleJoint.empty,
            mutableListOf()
        )

        /**
         * Static method to get the ExerciseType enum from an int obtained from the database
         * @param type Int representation of the ExerciseType
         * @return corresponding ExerciseType of the Int parameter
         */
        fun getExerciseType(type: Int): ExerciseType {
            return when(type){
                1 -> ExerciseType.STRENGTH
                2 -> ExerciseType.MOBILITY
                3 -> ExerciseType.STABILITY
                else -> ExerciseType.BLANK
            }
        }

        /**
         * Static method to get the secondary movers from a csv string
         * @param id Exercise ID
         * @param csvSecondaryMoversIDs CSV string for the all secondary movers (IDs)
         * @param csvSecondaryMoversNames CSV string of all the names of the secondary movers
         * @return MutableList of the secondary movers as MuscleJoints
         */
        fun getSecondaryMoversFromCSV(id: Int, csvSecondaryMoversIDs: String, csvSecondaryMoversNames: String): MutableList<MuscleJoint> {
            // if the id = 0 that means no exercise was found. If strSecondaryMovers is empty than no secondary movers are present
            // Either way, return a blank list is returned
            if (id == 0 || csvSecondaryMoversIDs == "0")
                return mutableListOf()
            val lstSecondaryMoversIDs = StaticFunctions.toListInt(csvSecondaryMoversIDs)
            val lstSecondaryMoversNames = csvSecondaryMoversNames.split(",")
            return lstSecondaryMoversIDs.mapIndexed { index, s -> MuscleJoint(s, lstSecondaryMoversNames[index]) }.toMutableList()
        }

        fun withCursor(it: Cursor): Exercise {
            val exerciseID = it.getInt(it.getColumnIndex(DBInfo.ExercisesTable.ID))
            val primaryMover = MuscleJoint(
                it.getInt(it.getColumnIndex(DBInfo.ExercisesTable.PRIMARY_MOVER)),
                it.getString(it.getColumnIndex(DBInfo.AliasesUsed.PRIMARY_MOVER_NAME))
            )
            return Exercise(
                exerciseID,
                it.getString(it.getColumnIndex(DBInfo.ExercisesTable.NAME)),
                getExerciseType(it.getInt(it.getColumnIndex(DBInfo.ExercisesTable.TYPE))),
                primaryMover,
                getSecondaryMoversFromCSV(
                    exerciseID,
                    it.getString(it.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_IDS)),
                    it.getString(it.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_NAMES))
                )
            )
        }
    }
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
    fun getLstSecondaryMovers(): MutableList<MuscleJoint> = secondaryMovers.toMutableList()

    //secondary movers operations
    fun addSecondaryMover(muscleJoint: MuscleJoint) = secondaryMovers.add(muscleJoint)
    fun removeSecondaryMover(muscleJoint: MuscleJoint) = secondaryMovers.remove(muscleJoint)
    fun clearSecondaryMovers() = secondaryMovers.clear()

    //database operations
    fun getInsertCommand(): String = "Insert Into Exercises(exercise_name, exercise_type, primary_mover_id) " +
                                     "Values('$name', ${type.num}, ${primaryMover.id});${getDatabaseSecondaryMovers()}"
    fun getUpdateCommand(): String = "Update Exercises " +
                                     "Set exercise_name = '$name', exercise_type = ${type.num}, primary_mover_id = ${primaryMover.id} " +
                                     "Where exercise_id = $id;" +
                                     "Delete From Secondary_Movers Where exercise_id = $id;" +
                                     getDatabaseSecondaryMovers()
    fun getDeleteCommand(): String = "Delete From Exercises Where exercise_id = $id;" +
                                     "Delete From Secondary_Movers Where exercise_id = $id"
}