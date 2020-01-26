package com.trainingapp.trainingassistant.objects

import com.trainingapp.trainingassistant.StaticFunctions
import java.lang.StringBuilder

/**
 * Object to hold sessions for a single day
 * @param sessions List of Session objects for a given date, initially sorted by time of the session within the day
 */
class Day (private var sessions: ArrayList<Session>) {

    init {
        sessions.sort()//sort sessions by time when object initialized
    }

    //Seemingly useless code
    /*fun removeSession(index: Int){
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
    }*/

    fun getSession(index: Int): Session = sessions[index]

    /**
     * Method to get all the client ids for sessions within the list. Used to figure out which clients cannot add a session to the day
     */
    fun getStrIDs(): String{
        return if (sessions.size > 0){
            val builder = StringBuilder()
            sessions.forEach { builder.append("${it.clientID},") }
            builder.deleteCharAt(builder.lastIndex)
            builder.toString()
        } else ""
    }

    fun getSessionCount(): Int = sessions.size

    /**
     * Method to check if a new session conflicts with existing sessions
     * If the time is being updated then isSameDate is true and the old session for that client is ignored
     * @param session Session to be added to the day
     * @param isSameDate only used if changing time (ie new session needs to ignore old session that it is going to replace)
     * @return true if conflict found, false if no conflict found
     */
    fun checkConflict(session: Session, isSameDate: Boolean = false): Boolean{
        var result = false
        for(daySession in sessions){
            val isSameClient = session.clientID == daySession.clientID
            if (!isSameClient)//if the session being analyzed is not for the same client as the given session
                result = StaticFunctions.compareTimeRanges(daySession.getTimeRange(), session.getTimeRange())//compare time ranges for the sessions to see if there is an overlap
            else if (isSameClient && !isSameDate)//if the session being analyzed is for the same client as the given session and the isSameDate flag is not set (ie if it breaks the rule of one session per day per client), set result as true
                result = true

            if (result)//if the analyzed session fails the tests, break the loop and return the result as true
                break
            //if the analyzed session passes the tests keep going with the loop until the end or a session fails the tests
        }
        return result
    }

    /**
     * Method to get the indices of any session that conflict with another. Uses the above function to check sessions for conflicts.
     * @return List of indices that have a conflict. Used to highlight conflicting sessions in the ScheduleFragment
     */
    fun getConflicts(): ArrayList<Int>{
        val conflicts = ArrayList<Int>()
        for (i in sessions.indices){
            if (checkConflict(sessions[i], true))
                conflicts.add(i)
        }
        return conflicts
    }
}