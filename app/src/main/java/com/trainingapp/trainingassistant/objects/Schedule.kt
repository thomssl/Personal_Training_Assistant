package com.trainingapp.trainingassistant.objects

import com.trainingapp.trainingassistant.StaticFunctions
import com.trainingapp.trainingassistant.enumerators.ScheduleType
import java.util.*

/**
 * Object to hold schedule details for a client
 * @param scheduleType schedule type of the client as defined by the Enum ScheduleType
 * @param days total number of days for a client for their given time frame. Will be non-zero unless scheduleType is NoSchedule
 * @param duration default duration for client schedule
 * @param daysList List of all times for the week. Only populated with non-zero values if scheduleType is constant
 * @param durationsList List of all durations for the week. Only populated with non-zero values if scheduleType is constant
 */
class Schedule (
    var scheduleType: ScheduleType,
    var days: Int,
    var duration: Int,
    var daysList: MutableList<Int>,
    var durationsList: MutableList<Int>
) {

    val sessionDays: List<ClientConflictData>
        get() = daysList.mapIndexed { index, it ->
            ClientConflictData(index,it until it + durationsList[index])
        }.filter { it.range.first != 0 }
    /**
     * Method to get the clients days for UI or database operations. Output accounts for ScheduleType
     * @return String value representing the days attribute of the client
     */
    fun getDays(): String{
        return when (scheduleType){
            ScheduleType.WEEKLY_CONSTANT -> {
                val builder = StringBuilder()
                daysList.forEachIndexed { index, it ->
                    if (it > 0)
                        builder.append("${StaticFunctions.NumToDay[index+1]}\n")
                }
                if (builder.isNotBlank())
                    builder.deleteCharAt(builder.lastIndex)
                builder.toString()
            }
            ScheduleType.WEEKLY_VARIABLE -> "$days/week"
            ScheduleType.MONTHLY_VARIABLE -> "$days/month"
            ScheduleType.NO_SCHEDULE -> ""
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
                //shows the start and end time
                daysList.forEachIndexed { index, i ->
                    if (i > 0)
                        builder.append("${getTime(i)}/${getTime(i+durationsList[index])}\n")
                }
                if (builder.isNotBlank())
                    builder.deleteCharAt(builder.lastIndex)
                return builder.toString()
            }
            ScheduleType.WEEKLY_VARIABLE ,ScheduleType.MONTHLY_VARIABLE,ScheduleType.NO_SCHEDULE -> ""
            ScheduleType.BLANK -> "Error"
        }
    }

    /**
     * Private Method to get the time as a formatted string given the time as an Int representation of the minutes in a day
     * @param time Time of the day as minutes in a day (Int)
     * @return String representation of the time sent formatted as am/pm
     */
    private fun getTime(time: Int): String {
        //minute of the hour
        val extra = time % 60
        //hour in the day (24 format)
        val hour = (time - extra) / 60
        //convert to am/pm String value
        return "${if(hour <= 12) hour else hour - 12}:${String.format(Locale.CANADA, "%02d", extra)} ${if(hour < 12) "am" else "pm"}"
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
        //minute of the hour
        val extra = daysList[index] % 60
        //hour in the day (24 format)
        val hour = (daysList[index] - extra) / 60
        extra.toString()
        //convert to am/pm String value
        return "${if(hour <= 12) hour else hour - 12}:${String.format(Locale.CANADA, "%02d", extra)} ${if(hour < 12) "am" else "pm"}"
    }

    fun getCheckClientConflictDays(): String {
        val builder = StringBuilder()
        daysList.forEachIndexed { index, i ->
            if (i > 0)
                builder.append("${StaticFunctions.NumToDay[index+1].toLowerCase(Locale.ROOT)} > 0 And ")
        }
        return builder.toString()
    }

    private fun getUpdateDays(): String{
        val builder = StringBuilder()
        daysList.forEachIndexed { index, i ->
            builder.append("${StaticFunctions.NumToDay[index+1].toLowerCase(Locale.ROOT)}=$i,")
        }
        if (builder.isNotBlank()) builder.deleteCharAt(builder.lastIndex)
        return builder.toString()
    }

    private fun getUpdateDurations(): String{
        val builder = StringBuilder()
        durationsList.forEachIndexed { index, i ->
            builder.append("${StaticFunctions.NumToDay[index+1].toLowerCase(Locale.ROOT)}_duration=$i,")
        }
        if (builder.isNotBlank()) builder.deleteCharAt(builder.lastIndex)
        return builder.toString()
    }

    fun getInsertCommand(): String = "${scheduleType.value}, $days, $duration,${daysList.joinToString()},${durationsList.joinToString()}"
    fun getUpdateCommand(): String = "schedule_type=${scheduleType.value},days=$days,duration=${duration},${getUpdateDays()},${getUpdateDurations()}"
}