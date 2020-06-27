package com.trainingapp.trainingassistant.objects

/**
 * Object to hold information gathered about a client
 * @param id Unique key given to the client when a new client is inserted. Used to differentiate session data in the database
 * @param name Unique string of text given as the client's name. User submitted but must be unique to make data more readable.
 *             User can use any form of differentiation they desire
 * @param schedule Schedule given to client. Used to define how a session is added or tracked for a given client. See Schedule class documentation
 * @param startDate String formatted as yyyy-MM-dd for the start date of the client (for constant or variable scheduled clients)
 *                  or '0' (for no schedule clients)
 * @param endDate String formatted as yyyy-MM-dd for the end date of the client (for constant or variable scheduled clients)
 *                or '0' (for no schedule clients)
 */
class Client (var id: Int, var name: String, var schedule: Schedule, var startDate: String, var endDate: String) {

    fun getDaysString(): String = schedule.getDays()
    fun getTimesString(): String = schedule.getTimes()
    fun getDuration(dateTime: String): Int = schedule.getDuration(dateTime)
    fun getStrSessionType(): String = schedule.scheduleType.text
    fun getStrTime(index: Int): String = schedule.getStrTime(index)

    //Database operations
    fun getInsertCommand(): String {
        return "Insert Into Clients(client_name, start_date, end_date, schedule_type, days, duration, sun, mon, tue, wed, thu, fri, sat, " +
                "sun_duration, mon_duration, tue_duration, wed_duration, thu_duration, fri_duration, sat_duration) " +
                "Values('$name', '$startDate', '$endDate',${schedule.getInsertCommand()});"
    }
    fun getUpdateCommand(): String {
        return  "Update Clients " +
                "Set client_name='$name',start_date='$startDate',end_date='$endDate',${schedule.getUpdateCommand()} " +
                "Where client_id=$id;"
    }
    fun getDeleteCommand():String {
        return  "Delete From Clients         Where client_id = $id; " +
                "Delete From Session_Changes Where client_id = $id; " +
                "Delete From Session_log     Where client_id = $id;"
    }
}