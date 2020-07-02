package com.trainingapp.trainingassistant.objects

import android.database.Cursor
import com.trainingapp.trainingassistant.database.DBInfo
import com.trainingapp.trainingassistant.enumerators.ExerciseType

/**
 * Object to hold hold gathered data about an exercise within a session. Contains an Exercise object and adds sets, reps, day and order
 * @param id Unique key given to the exercise when a new exercise is inserted. Used to differentiate exercises within sessions
 * @param name Unique string of text given as the exercise's name. User submitted but must be unique to differentiate exercises
 * @param type ExerciseType enum value given to the exercise. Used to define what the primary and secondary movers are (Muscles or Joints)
 * @param primaryMover MuscleJoint object containing the name and id to the primary mover
 * @param secondaryMovers List of the MuscleJoint objects that contain the names and ids of the secondary movers
 * @param sets String representation of the sets, usually numbers but the user has full discretion and can use characters that follow the rules
 * @param reps String representation of the reps, usually numbers but the user has full discretion and can use characters that follow the rules
 * @param day Int representation of the day of the week for this exercises
 * @param order number of occurrence within the session, can repeat for concurrent sets
 */
class ExerciseProgram(
    var id: Int,
    var name: String,
    var type: ExerciseType,
    private val primaryMover: MuscleJoint,
    private val secondaryMovers: MutableList<MuscleJoint>,
    var sets: String,
    var reps: String,
    var day: Int,
    var order: Int
): Comparable<ExerciseProgram>{

    companion object {
        fun empty(exercise: Exercise) = ExerciseProgram(
            exercise,
            "",
            "",
            0,
            0
        )

        fun withCursor(it: Cursor): ExerciseProgram {
            val exerciseID = it.getInt(it.getColumnIndex(DBInfo.ExercisesTable.ID))
            return ExerciseProgram(
                exerciseID,
                it.getString(it.getColumnIndex(DBInfo.ExercisesTable.NAME)),
                Exercise.getExerciseType(it.getInt(it.getColumnIndex(DBInfo.ExercisesTable.TYPE))),
                MuscleJoint(
                    it.getInt(it.getColumnIndex(DBInfo.ExercisesTable.PRIMARY_MOVER)),
                    it.getString(it.getColumnIndex(DBInfo.AliasesUsed.PRIMARY_MOVER_NAME))
                ),
                Exercise.getSecondaryMoversFromCSV(
                    exerciseID,
                    it.getString(it.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_IDS)),
                    it.getString(it.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_NAMES))
                ),
                it.getString(it.getColumnIndex(DBInfo.ProgramExercisesTable.SETS)),
                it.getString(it.getColumnIndex(DBInfo.ProgramExercisesTable.REPS)),
                it.getInt(it.getColumnIndex(DBInfo.ProgramExercisesTable.DAY)),
                it.getInt(it.getColumnIndex(DBInfo.ProgramExercisesTable.EXERCISE_ORDER))
            )
        }
    }

    /**
     * Secondary constructor to populate an ExerciseProgram with an Exercise, sets, reps, day and order instead of all the base data.
     * Used most often due to simplicity
     */
    constructor(exercise: Exercise, sets: String, reps: String, day: Int, order: Int):
            this(exercise.id, exercise.name, exercise.type, exercise.primaryMover, exercise.getLstSecondaryMovers(), sets, reps, day, order)

    /**
     * Method used to access if the object has been populated properly
     * @return true if sets contains data, false if no data for sets present
     */
    fun hasData(): Boolean = sets.isNotBlank()

    /**
     * Method to get the Exercise object used to initialize the object
     * @return Exercise object contained within this object
     */
    fun getExercise(): Exercise = Exercise(id, name, type, primaryMover, secondaryMovers)

    /**
     * Method to get the ExerciseSession object for this object
     * @return ExerciseSession object within this object, resistance is blank
     */
    fun getExerciseSession(): ExerciseSession = ExerciseSession(getExercise(), sets, reps, "", order)

    override fun compareTo(other: ExerciseProgram): Int = this.order - other.order
}