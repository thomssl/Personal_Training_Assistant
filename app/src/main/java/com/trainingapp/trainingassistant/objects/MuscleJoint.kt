package com.trainingapp.trainingassistant.objects

import android.database.Cursor
import com.trainingapp.trainingassistant.database.DBInfo

/**
 * Data object to hold the id and name of a muscle or joint
 */
data class MuscleJoint(val id: Int, val name: String){
    companion object {
        val empty = MuscleJoint(0,"")

        fun withCursor(it: Cursor, isMuscle: Boolean): MuscleJoint {
            return MuscleJoint (
                it.getInt(it.getColumnIndex(if (isMuscle) DBInfo.MusclesTable.ID else DBInfo.JointsTable.ID)),
                it.getString(it.getColumnIndex(if (isMuscle) DBInfo.MusclesTable.NAME else DBInfo.JointsTable.NAME))
            )
        }
    }
}