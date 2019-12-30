package com.trainingapp.personaltrainingassistant.objects

import com.trainingapp.personaltrainingassistant.StaticFunctions
import com.trainingapp.personaltrainingassistant.enumerators.ExerciseType
import java.lang.StringBuilder


class Exercise(val id: Int, var name: String, var type: ExerciseType, var primaryMover: Int, var strPrimaryMover: String, var strSecondaryMovers: String) {

    var secondaryMovers: ArrayList<Int> = StaticFunctions.toArrayListInt(strSecondaryMovers)
    var strSecondaryMoversList = ArrayList<String>()

    fun getSecondaryMoversNames(): String{
        val builder = StringBuilder()
        if (strSecondaryMoversList.size > 0) {
            strSecondaryMoversList.forEach { builder.append(it);builder.append("\n") }
            builder.deleteCharAt(builder.lastIndex)
        }
        return builder.toString()
    }

    fun addSecondaryMover(id: Int, name: String){
        if (secondaryMovers[0] == 0){
            strSecondaryMovers = id.toString()
        } else {
            strSecondaryMovers += ",$id"
        }
        secondaryMovers.add(id)
        strSecondaryMoversList.add(name)
    }

    fun removeSecondaryMover(id: Int, name: String){
        secondaryMovers.remove(id)
        strSecondaryMoversList.remove(name)
        strSecondaryMovers = if (secondaryMovers.size == 0) ""
        else {
            val builder = StringBuilder()
            secondaryMovers.forEach { builder.append("$it,") }
            builder.deleteCharAt(builder.lastIndex)
            builder.toString()
        }
    }

    fun clearSecondaryMovers(){
        secondaryMovers.clear()
        strSecondaryMovers = ""
        strSecondaryMoversList.clear()
    }

    fun getInsertCommand(): String = "Insert Into Exercises(name, type, primary_mover, secondary_movers) Values('$name', ${type.value}, $primaryMover, '$strSecondaryMovers')"
    fun getUpdateCommand(): String = "Update Exercises Set name = '$name', type = ${type.value}, primary_mover = $primaryMover, secondary_movers = '$strSecondaryMovers' Where id = $id"
    fun getDeleteCommand(): String = "Delete From Exercises Where id = $id"
}