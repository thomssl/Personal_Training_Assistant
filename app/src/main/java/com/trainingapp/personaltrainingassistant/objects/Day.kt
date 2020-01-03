package com.trainingapp.personaltrainingassistant.objects

import com.trainingapp.personaltrainingassistant.StaticFunctions
import java.lang.StringBuilder

class Day (private var sessions: ArrayList<Session>) {
    init {
        sessions.sort()
    }

    fun removeSession(index: Int){
        sessions.removeAt(index)
        sessions.sort()
    }

    fun removeSession(session: Session){
        sessions.remove(session)
        sessions.sort()
    }

    fun addSession(session: Session): Boolean{
        return if (!checkConflict(session)) {
            sessions.add(session)
            sessions.sort()
            true
        } else false
    }

    fun getSession(index: Int): Session = sessions[index]

    fun printData(): String{
        val builder = StringBuilder()
        for (session in sessions){
            builder.append("${session.clientName} ${StaticFunctions.getStrDateTime(
                session.date
            )}")
        }
        return if (builder.isNotEmpty()) builder.toString() else "no entries"
    }

    fun getStrIDs(): String{
        return if (sessions.size > 0){
            val builder = StringBuilder()
            sessions.forEach { builder.append("${it.clientID},") }
            builder.deleteCharAt(builder.lastIndex)
            builder.toString()
        } else ""
    }

    fun getSessionCount(): Int = sessions.size

    fun checkConflict(session: Session, isSameDate: Boolean = false): Boolean{
        var result = false
        for(daySession in sessions){
            val isSameClient = session.clientID == daySession.clientID
            if (!isSameClient)
                result = StaticFunctions.compareTimeRanges(daySession.getTimeRange(), session.getTimeRange())
            else if (isSameClient &&  !isSameDate)
                result = true

            if (result)
                break
        }
        return result
    }

    fun getConflicts(): ArrayList<Int>{
        val conflicts = ArrayList<Int>()
        for (i in sessions.indices){
            if (checkConflict(sessions[i], true))
                conflicts.add(i)
        }
        return conflicts
    }
}