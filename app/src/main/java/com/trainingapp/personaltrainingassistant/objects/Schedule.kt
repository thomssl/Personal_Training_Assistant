package com.trainingapp.personaltrainingassistant.objects

import com.trainingapp.personaltrainingassistant.StaticFunctions
import com.trainingapp.personaltrainingassistant.enumerators.ScheduleType
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

/**
 * Object to hold schedule details for a client
 * @param scheduleType schedule type of the client as defined by the Enum ScheduleType
 * @param days total number of days for a client for their given time frame. Will be non-zero unless scheduleType is NoSchedule
 * @param duration default duration for client schedule
 * @param daysList List of all times for the week. Only populated with non-zero values if scheduleType is constant
 * @param durationsList List of all durations for the week. Only populated with non-zero values if scheduleType is constant
 */
class Schedule (var scheduleType: ScheduleType, var days: Int, var duration: Int, var daysList: ArrayList<Int>, var durationsList: ArrayList<Int>) {

    /**
     * Method to get the clients days for UI or database operations. Output accounts for ScheduleType
     * @return String value representing the days attribute of the client
     */
    fun getDays(): String{
        return when (scheduleType){
            ScheduleType.WEEKLY_CONSTANT -> {
                val builder = StringBuilder()
                daysList.forEachIndexed { index, i -> if (i > 0) builder.append("${builder.append(StaticFunctions.NumToDay[index])}\n") }
                if (builder.isNotBlank())
                    builder.deleteCharAt(builder.lastIndex)
                builder.toString()
            }
            ScheduleType.WEEKLY_VARIABLE ,ScheduleType.MONTHLY_VARIABLE, ScheduleType.NO_SCHEDULE -> days.toString()
            ScheduleType.BLANK -> "Error"
        }
    }

    /**
     * Method to get the clients times for UI or database operations. Output accounts for ScheduleType
     * @return String value representing the times attribute of the client
     */
    fun getTimes(): String {
        return when (scheduleType){
            ScheduleType.WEEKLY_CONSTANT -> {
                val builder = StringBuilder()
                daysList.forEachIndexed { index, i -> if (i > 0) builder.append("${getTime(i)}/${getTime(i+durationsList[index])}\n") }//shows the start and end time
                if (builder.isNotBlank())
                    builder.deleteCharAt(builder.lastIndex)
                return builder.toString()
            }
            ScheduleType.NO_SCHEDULE,ScheduleType.MONTHLY_VARIABLE,ScheduleType.WEEKLY_VARIABLE -> ""
            ScheduleType.BLANK -> "Error"
        }
    }

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
     * Method to get the duration depending upon the dateTime of a session
     * If ScheduleType is constant, get the day of the week from the dateTime given
     * If ScheduleType is non-constant, return the first index of the durations list
     */
    fun getDuration(dateTime: String): Int{
        return if (scheduleType == ScheduleType.WEEKLY_CONSTANT) {
            val calendar = StaticFunctions.getDate(dateTime)
            val index = calendar[Calendar.DAY_OF_WEEK] - 1
            if (index >= 0) durationsList[index] else 0
        } else duration
    }

    /**
     * Method to get the time as a formatted string given the time as an Int representation of the minutes in a day
     * @param index position of the time needed within the times list
     * @return String representation of the time at the index sent formatted as am/pm
     */
    fun getStrTime(index: Int): String{
        val extra = daysList[index] % 60//minute of the hour
        val hour = (daysList[index] - extra) / 60//hour in the day (24 format)
        return "${if(hour <= 12) hour else hour - 12}:${String.format(Locale.CANADA, "%02d", extra)} ${if(hour < 12) "am" else "pm"}"//convert to am/pm String value
    }

    fun getInsertCommand(id: Int): String = "Insert Into Schedules(client_id, schedule_type, days, duration, sun, mon, tue, wed, thu, fri, sat, sun, sun_duration, mon_duration, tue_duration, wed_duration, thu_duration, fri_duration, sat_duration) " +
            "Values($id, $scheduleType, $days, $duration, ${daysList[0]}, ${daysList[1]}, ${daysList[2]}, ${daysList[3]}, ${daysList[4]}, ${daysList[5]}, ${daysList[6]}, ${durationsList[0]}, ${durationsList[1]}, ${durationsList[2]}, " +
            "${durationsList[3]}, ${durationsList[4]}, ${durationsList[5]}, ${durationsList[6]})"
    fun getUpdateCommand(id: Int): String = "Update Schedules Set schedule_type = $scheduleType, days = $days, duration = ${duration}, sun = ${daysList[0]}, mon = ${daysList[1]}, tue = ${daysList[2]}, wed = ${daysList[3]}, thu = ${daysList[4]}," +
            " fri = ${daysList[5]}, sat = ${daysList[6]}, sun = ${daysList[0]}, sun_duration = ${durationsList[0]}, mon_duration = ${durationsList[1]}, tue_duration = ${durationsList[2]}, wed_duration = ${durationsList[3]}," +
            " thu_duration = ${durationsList[4]}, fri_duration = ${durationsList[5]}, sat_duration = ${durationsList[6]} Where client_id = $id"
}