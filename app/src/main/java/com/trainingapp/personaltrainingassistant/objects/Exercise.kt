package com.trainingapp.personaltrainingassistant.objects

import com.trainingapp.personaltrainingassistant.StaticFunctions
import com.trainingapp.personaltrainingassistant.enumerators.ExerciseType
import java.lang.StringBuilder


class Exercise(val id: Int, var name: String, var type: ExerciseType, var primaryMover: MuscleJoint, private val secondaryMovers: ArrayList<MuscleJoint>) {

    fun getSecondaryMoversNames(): String{
        val builder = StringBuilder()
        if (secondaryMovers.size > 0) {
            secondaryMovers.forEach { builder.append(it.name);builder.append("\n") }
            builder.deleteCharAt(builder.lastIndex)
        }
        return builder.toString()
    }

    private fun getStrSecondaryMovers(): String{
        val builder = StringBuilder()
        if (secondaryMovers.size > 0) {
            secondaryMovers.forEach { builder.append(it.id);builder.append(',') }
            builder.deleteCharAt(builder.lastIndex)
        }
        return builder.toString()
    }

    fun getLstSecondaryMovers(): ArrayList<MuscleJoint> = ArrayList(secondaryMovers.toList())

    fun addSecondaryMover(muscleJoint: MuscleJoint){
        secondaryMovers.add(muscleJoint)
    }

    fun removeSecondaryMover(muscleJoint: MuscleJoint){
        secondaryMovers.remove(muscleJoint)
    }

    fun clearSecondaryMovers(){
        secondaryMovers.clear()
    }

    fun getInsertCommand(): String = "Insert Into Exercises(name, type, primary_mover, secondary_movers) Values('$name', ${type.value}, ${primaryMover.id}, '${getStrSecondaryMovers()}')"
    fun getUpdateCommand(): String = "Update Exercises Set name = '$name', type = ${type.value}, primary_mover = ${primaryMover.id}, secondary_movers = '${getStrSecondaryMovers()}' Where id = $id"
    fun getDeleteCommand(): String = "Delete From Exercises Where id = $id"
}