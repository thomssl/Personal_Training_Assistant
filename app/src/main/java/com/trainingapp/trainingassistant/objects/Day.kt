package com.trainingapp.trainingassistant.objects

import com.trainingapp.trainingassistant.StaticFunctions

/**
 * Object to hold sessions for a single day
 * @param sessions List of Session objects for a given date, initially sorted by time of the session within the day
 */
class Day (private val sessions: MutableList<Session>) {

    init {
        //sort sessions by time when object initialized
        sessions.sort()
    }

    fun getSession(index: Int): Session = sessions[index]

    /**
     * Method to get all the client ids for sessions within the list. Used to figure out which clients cannot add a session to the day
     */
    val strIDs: String
        get() {
            return if (sessions.size > 0) {
                val builder = StringBuilder()
                sessions.forEach { builder.append("${it.clientID},") }
                builder.deleteCharAt(builder.lastIndex)
                builder.toString()
            } else ""
        }

    val sessionCount: Int
        get() = sessions.size

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
            //if the session being analyzed is not for the same client as the given session
            if (!isSameClient)
                result = StaticFunctions.compareTimeRanges(daySession.timeRange, session.timeRange)
            //if the session being analyzed is for the same client as the given session and the isSameDate flag is not set (ie if it breaks the
            //rule of one session per day per client), set result as true
            else if (isSameClient && !isSameDate)
                result = true

            //if the analyzed session fails the tests, break the loop and return the result as true
            if (result)
                break
            //if the analyzed session passes the tests keep going with the loop until the end or a session fails the tests
        }
        return result
    }

    /**
     * Method to get the indices of any session that conflict with another. Uses the above function to check sessions for conflicts.
     * @return List of indices that have a conflict. Used to highlight conflicting sessions in the ScheduleFragment
     */
    val conflicts: List<Int>
        get() {
            return sessions.filter { session -> checkConflict(session, true) }.mapIndexed { index, _ -> index }
        }
}