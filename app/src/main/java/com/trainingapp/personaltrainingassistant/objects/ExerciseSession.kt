package com.trainingapp.personaltrainingassistant.objects

import com.trainingapp.personaltrainingassistant.StaticFunctions
import com.trainingapp.personaltrainingassistant.enumerators.ExerciseType
import java.lang.StringBuilder

class ExerciseSession(var id: Int, var name: String, var type: ExerciseType, private val primaryMover: MuscleJoint, private val secondaryMovers: ArrayList<MuscleJoint>, var sets: String, var reps: String, var resistance: String, var order: Int): Comparable<ExerciseSession>{

    constructor(exercise: Exercise, sets: String, reps: String, resistance: String, order: Int): this(exercise.id, exercise.name, exercise.type, exercise.primaryMover, exercise.getLstSecondaryMovers(), sets, reps, resistance, order)

    fun getSecondaryMoversString(): String{
        val builder = StringBuilder()
        for (mover in secondaryMovers) {
            builder.append(mover.id)
            builder.append(',')
        }
        builder.deleteCharAt(builder.lastIndex)
        return builder.toString()
    }

    fun hasData(): Boolean = sets.isNotEmpty()

    fun getExercise(): Exercise = Exercise(id, name, type, primaryMover, secondaryMovers)

    override fun compareTo(other: ExerciseSession): Int = this.order - other.order
}