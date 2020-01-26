package com.trainingapp.trainingassistant.objects

import com.trainingapp.trainingassistant.StaticFunctions
import com.trainingapp.trainingassistant.enumerators.ScheduleType
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

/**
 * Object to hold information gathered about a client
 * @param id Unique key given to the client when a new client is inserted. Used to differentiate session data in the database
 * @param name Unique string of text given as the client's name. User submitted but must be unique to make data more readable. User can use any form of differentiation they desire
 * @param scheduleType ScheduleType enum value given to client. Used to define how a session is added or tracked for a given client
 * @param days String as a single non-zero Integer (for variable ScheduleType), a csv that is split into a list (for constant ScheduleType) or zero (for no schedule clients)
 * @param times String as a single non-zero Integer (for variable ScheduleType), a csv that is split into a list (for constant ScheduleType) or zero (for no schedule clients)
 * @param durations String as a single Integer (for non-constant ScheduleType) or a csv that is split into a list (for constant ScheduleType)
 * @param startDate String formatted as yyyy-MM-dd for the start date of the client (for constant or variable scheduled clients) or '0' (for no schedule clients)
 * @param endDate String formatted as yyyy-MM-dd for the end date of the client (for constant or variable scheduled clients) or '0' (for no schedule clients)
 */
class Client (var id: Int, var name: String, var scheduleType: ScheduleType, days: String, times: String, durations: String, var startDate: String, var endDate: String) {

    //split database csv values and convert the given list to an ArrayList<Int>. Size = 1 for non-constant ScheduleTypes
    var days: ArrayList<Int> = StaticFunctions.toArrayListInt(days)
    var times: ArrayList<Int> = StaticFunctions.toArrayListInt(times)
    var durations: ArrayList<Int> = StaticFunctions.toArrayListInt(durations)

    /**
     * Method to get the clients days for UI or database operations. Output accounts for ScheduleType
     * @param isDatabase value denotes if the output will be used for database operations
     * @return String value representing the days attribute of the client
     */
    fun getDaysString(isDatabase: Boolean): String{
        return when (scheduleType){
            ScheduleType.WEEKLY_CONSTANT -> {
                val builder = StringBuilder()
                for (day in days) {
                    if (isDatabase) {
                        builder.append(day)
                        builder.append(',')
                    }
                    else {
                        builder.append(StaticFunctions.NumToDay[day])
                        builder.append("\n")
                    }
                }
                builder.deleteCharAt(builder.lastIndex)
                builder.toString()
            }
            ScheduleType.WEEKLY_VARIABLE,ScheduleType.MONTHLY_VARIABLE -> days[0].toString()
            ScheduleType.NO_SCHEDULE -> if (isDatabase) days[0].toString() else ""
            ScheduleType.BLANK -> "Error"
        }
    }

    /**
     * Method to get the clients times for UI or database operations. Output accounts for ScheduleType
     * @param isDatabase value denotes if the output will be used for database operations
     * @return String value representing the times attribute of the client
     */
    fun getTimesString(isDatabase: Boolean): String{
        return when (scheduleType){
            ScheduleType.WEEKLY_CONSTANT -> {
                val builder = StringBuilder()
                for(index in times.indices){
                    if (isDatabase) {
                        builder.append(times[index])
                        builder.append(',')
                    }
                    else {
                        builder.append("${getTime(times[index])}/${getTime(times[index]+durations[index])}")//shows the start and end time
                        builder.append("\n")
                    }
                }
                builder.deleteCharAt(builder.lastIndex)
                return builder.toString()
            }
            ScheduleType.NO_SCHEDULE,ScheduleType.MONTHLY_VARIABLE,ScheduleType.WEEKLY_VARIABLE -> if (isDatabase) times[0].toString() else ""
            ScheduleType.BLANK -> "Error"
        }
    }

    /**
     * Private Method to get the clients times for database operations
     * @return String value representing the durations attribute of the client
     */
    private fun getDurationsString(): String{
        val builder = StringBuilder()
        for(duration in durations){
            builder.append(duration)
            builder.append(',')
        }
        builder.deleteCharAt(builder.lastIndex)
        return builder.toString()
    }

    /**
     * Method to get the duration depending upon the dateTime of a session
     * If ScheduleType is constant, get the day of the week from the dateTime given
     * If ScheduleType is non-constant, return the first index of the durations list
     */
    fun getDuration(dateTime: String): Int{
        return if (scheduleType == ScheduleType.WEEKLY_CONSTANT) {
            val calendar = StaticFunctions.getDate(dateTime)
            val index = days.indexOf(calendar[Calendar.DAY_OF_WEEK])
            if (index >= 0) durations[index] else 0
        } else
            durations[0]
    }

    fun getStrSessionType(): String = scheduleType.text

    /**
     * Private Method to get the time as a formatted string given the time as an Int representation of the minutes in a day
     * @param time Time of the day as minutes in a day (Int)
     * @return String representation of the time sent formatted as am/pm
     */
    private fun getTime(time: Int): String {
        val extra = time % 60//minute of the hour
        val hour = (time - extra) / 60//hour in the day (24 format)
        return "${if(hour <= 12) hour else hour - 12}:${String.format(Locale.CANADA, "%02d", extra)} ${if(hour < 12) "am" else "pm"}"//convert to am/pm String value
    }

    /**
     * Private Method to get the time as a formatted string given the time as an Int representation of the minutes in a day
     * @param index position of the time needed within the times list
     * @return String representation of the time at the index sent formatted as am/pm
     */
    fun getStrTime(index: Int): String{
        val extra = times[index] % 60//minute of the hour
        val hour = (times[index] - extra) / 60//hour in the day (24 format)
        return "${if(hour <= 12) hour else hour - 12}:${String.format(Locale.CANADA, "%02d", extra)} ${if(hour < 12) "am" else "pm"}"//convert to am/pm String value
    }

    //Database operations
    fun getInsertCommand(): String = "Insert Into Clients(client_name, schedule_type, days, times, durations, start_date, end_date) Values('$name', ${scheduleType.value}, '${getDaysString(true)}', '${getTimesString(true)}', '${getDurationsString()}', '$startDate', '$endDate')"
    fun getUpdateCommand(): String = "Update Clients Set client_name = '$name', schedule_type = ${scheduleType.value}, days = '${getDaysString(true)}', times = '${getTimesString(true)}', durations = '${getDurationsString()}', start_date = '$startDate', end_date = '$endDate' Where client_id = $id"
    fun getDeleteCommand():String = "Delete From Clients Where client_id = $id; Delete From Session_Changes Where client_id = $id; Delete From Session_log Where client_id = $id"
}