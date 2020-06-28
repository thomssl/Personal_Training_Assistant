package com.trainingapp.trainingassistant.objects

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
    var id: Int,
    var name: String,
    var type: ExerciseType,
    private val primaryMover: MuscleJoint,
    private val secondaryMovers: MutableList<MuscleJoint>,
    var sets: String,
    var reps: String,
    var resistance: String,
    var order: Int
): Comparable<ExerciseSession>{

    companion object {
        fun empty(exercise: Exercise) = ExerciseSession(
            exercise,
            "",
            "",
            "",
            0
        )
    }
    /**
     * Secondary constructor to populate an ExerciseSession with an Exercise, sets, reps, resistance and order instead of all the base data.
     * Used most often due to simplicity
     */
    constructor(exercise: Exercise, sets: String, reps: String, resistance: String, order: Int):
            this(exercise.id, exercise.name, exercise.type, exercise.primaryMover, exercise.getLstSecondaryMovers(), sets, reps, resistance, order)

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

    override fun compareTo(other: ExerciseSession): Int = this.order - other.order
}