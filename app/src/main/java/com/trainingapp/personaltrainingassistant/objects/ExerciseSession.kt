package com.trainingapp.personaltrainingassistant.objects

import com.trainingapp.personaltrainingassistant.StaticFunctions
import com.trainingapp.personaltrainingassistant.enumerators.ExerciseType
import java.lang.StringBuilder

class ExerciseSession(var id: Int, var name: String, var type: ExerciseType, private val primaryMover: Int, private val strPrimaryMover: String, private val strSecondaryMovers: String, var sets: String, var reps: String, var resistance: String, var order: Int): Comparable<ExerciseSession>{

    constructor(exercise: Exercise, sets: String, reps: String, resistance: String, order: Int): this(exercise.id, exercise.name, exercise.type, exercise.primaryMover, exercise.strPrimaryMover, exercise.strSecondaryMovers, sets, reps, resistance, order)

    private var secondaryMovers: ArrayList<Int> = StaticFunctions.toArrayListInt(strSecondaryMovers)

    fun getSecondaryMoversString(): String{
        val builder = StringBuilder()
        for (mover in secondaryMovers) {
            builder.append(mover)
            builder.append(',')
        }
        builder.deleteCharAt(builder.lastIndex)
        return builder.toString()
    }

    fun hasData(): Boolean = sets.isNotEmpty()

    fun getExercise(): Exercise = Exercise(id, name, type, primaryMover, strPrimaryMover, strSecondaryMovers)

    override fun compareTo(other: ExerciseSession): Int = this.order - other.order
}