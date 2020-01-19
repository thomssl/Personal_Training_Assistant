package com.trainingapp.personaltrainingassistant.objects

/**
 * Object to hold information gathered about a client
 * @param id Unique key given to the client when a new client is inserted. Used to differentiate session data in the database
 * @param name Unique string of text given as the client's name. User submitted but must be unique to make data more readable. User can use any form of differentiation they desire
 * @param schedule Schedule given to client. Used to define how a session is added or tracked for a given client. See Schedule class documentation
 * @param startDate String formatted as yyyy-MM-dd for the start date of the client (for constant or variable scheduled clients) or '0' (for no schedule clients)
 * @param endDate String formatted as yyyy-MM-dd for the end date of the client (for constant or variable scheduled clients) or '0' (for no schedule clients)
 */
class Client2 (var id: Int, var name: String, var schedule: Schedule, var startDate: String, var endDate: String) {

    fun getDaysString(): String = schedule.getDays()
    fun getTimesString(): String = schedule.getTimes()
    fun getDuration(dateTime: String): Int = schedule.getDuration(dateTime)
    fun getStrSessionType(): String = schedule.scheduleType.text
    fun getStrTime(index: Int): String = schedule.getStrTime(index)

    //Database operations
    fun getInsertCommand(): String = "Insert Into Clients(client_name, start_date, end_date) Values('$name', '$startDate', '$endDate'); ${schedule.getInsertCommand(id)}"
    fun getUpdateCommand(): String = "Update Clients Set client_name = '$name', start_date = '$startDate', end_date = '$endDate' Where client_id = $id; ${schedule.getUpdateCommand(id)}"
    fun getDeleteCommand():String = "Delete From Clients Where client_id = $id; Delete From Session_Changes Where client_id = $id; Delete From Session_log Where client_id = $id; Delete From Schedules Where client_id = $id"
}