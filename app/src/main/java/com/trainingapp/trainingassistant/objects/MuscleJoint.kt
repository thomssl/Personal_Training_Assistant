package com.trainingapp.trainingassistant.objects

/**
 * Data object to hold the id and name of a muscle or joint
 */
data class MuscleJoint(var id: Int, var name: String){
    companion object {
        val empty = MuscleJoint(0,"")
    }
}