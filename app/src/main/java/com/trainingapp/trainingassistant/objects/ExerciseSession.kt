package com.trainingapp.trainingassistant.objects

import android.database.Cursor
import com.trainingapp.trainingassistant.database.DBInfo
import com.trainingapp.trainingassistant.enumerators.ExerciseType

/**
 * Object to hold hold gathered data about an exercise within a session. Contains an Exercise object and adds sets, reps, resistance and order
 * @param id Unique key given to the exercise when a new exercise is inserted. Used to differentiate exercises within sessions
 * @param name Unique string of text given as the exercise's name. User submitted but must be unique to differentiate exercises
 * @param type ExerciseType enum value given to the exercise. Used to define what the primary and secondary movers are (Muscles or Joints)
 * @param primaryMover MuscleJoint object containing the name and id to the primary mover
 * @param secondaryMovers List of the MuscleJoint objects that contain the names and ids of the secondary movers
 * @param sets String representation of the sets, usually numbers but the user has full discretion to use characters that follow the rules
 * @param reps String representation of the reps, usually numbers but the user has full discretion to use characters that follow the rules
 * @param resistance String representation of the resistance, usually numbers but the user has full discretion to use characters that follow the rules
 * @param order number of occurrence within the session, can repeat for concurrent sets
 */
class ExerciseSession(
    val id: Int,
    val name: String,
    val type: ExerciseType,
    private val primaryMover: MuscleJoint,
    private val secondaryMovers: MutableList<MuscleJoint>,
    val sets: String,
    val reps: String,
    val resistance: String,
    val order: Int
): Comparable<ExerciseSession> {

    companion object {
        fun empty(exercise: Exercise) = ExerciseSession(
            exercise,
            "",
            "",
            "",
            0
        )

        fun withCursor(it: Cursor): ExerciseSession {
            val exerciseID = it.getInt(it.getColumnIndex(DBInfo.ExercisesTable.ID))
            return ExerciseSession(
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
                it.getString(it.getColumnIndex(DBInfo.SessionExercisesTable.SETS)),
                it.getString(it.getColumnIndex(DBInfo.SessionExercisesTable.REPS)),
                it.getString(it.getColumnIndex(DBInfo.SessionExercisesTable.RESISTANCE)),
                it.getInt(it.getColumnIndex(DBInfo.SessionExercisesTable.EXERCISE_ORDER))
            )
        }

        fun withCursor(exercise: Exercise, it: Cursor): ExerciseSession {
            return ExerciseSession(
                exercise,
                it.getString(it.getColumnIndex(DBInfo.SessionExercisesTable.SETS)),
                it.getString(it.getColumnIndex(DBInfo.SessionExercisesTable.REPS)),
                it.getString(it.getColumnIndex(DBInfo.SessionExercisesTable.RESISTANCE)),
                it.getInt(it.getColumnIndex(DBInfo.SessionExercisesTable.EXERCISE_ORDER))
            )
        }
    }
    /**
     * Secondary constructor to populate an ExerciseSession with an Exercise, sets, reps, resistance and order instead of all the base data.
     * Used most often due to simplicity
     */
    constructor(exercise: Exercise, sets: String, reps: String, resistance: String, order: Int):
            this(exercise.id, exercise.name, exercise.type, exercise.primaryMover, exercise.lstSecondaryMovers, sets, reps, resistance, order)

    /**
     * Property used to access if the object has been populated properly
     * @return true if sets contains data, false if no data for sets present
     */
    val hasData: Boolean
        get() = sets.isNotBlank()

    /**
     * Property to get the Exercise object used to initialize the object
     * @return Exercise object contained within this object
     */
    val exercise: Exercise
        get() = Exercise(id, name, type, primaryMover, secondaryMovers)

    override fun compareTo(other: ExerciseSession): Int = this.order - other.order
}