package com.trainingapp.personaltrainingassistant.objects

import com.trainingapp.personaltrainingassistant.StaticFunctions
import com.trainingapp.personaltrainingassistant.enumerators.ScheduleType
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class Client (var id: Int, var name: String, var scheduleType: ScheduleType, days: String, times: String, durations: String, var startDate: String, var endDate: String) {

    var days: ArrayList<Int> = StaticFunctions.toArrayListInt(days) //split database csv of days and convert the given list to an ArrayList<Int>
    var times: ArrayList<Int> = StaticFunctions.toArrayListInt(times) //split database csv of times and convert the given list to an ArrayList<Int>
    var durations: ArrayList<Int> = StaticFunctions.toArrayListInt(durations) //split database csv of times and convert the given list to an ArrayList<Int>
    //private var startDate = StaticFunctions.getDate(startDate) //convert database string of startDate into Calendar object
    //private var endDate = StaticFunctions.getDate(endDate) //convert database string of endDate into Calendar object

    //return clients days as a string depending on if the output is required for a database command
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
        }
    }

    //return clients times as a string depending on if the output is required for a database command
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
                        builder.append("${getTime(times[index])}/${getTime(times[index]+durations[index])}")
                        builder.append("\n")
                    }
                }
                builder.deleteCharAt(builder.lastIndex)
                return builder.toString()
            }
            ScheduleType.NO_SCHEDULE,ScheduleType.MONTHLY_VARIABLE,ScheduleType.WEEKLY_VARIABLE -> if (isDatabase) times[0].toString() else ""
        }
    }

    //return clients times as a string depending on if the output is required for a database command
    private fun getDurationsString(): String{
        val builder = StringBuilder()
        for(duration in durations){
            builder.append(duration)
            builder.append(',')
        }
        builder.deleteCharAt(builder.lastIndex)
        return builder.toString()
    }

    fun getDuration(dateTime: String): Int{
        val calendar = StaticFunctions.getDate(dateTime)
        return durations[days.indexOf(calendar[Calendar.DAY_OF_WEEK])]
    }

    fun getStrSessionType(): String = scheduleType.text

    private fun getTime(time: Int): String {
        val extra = time % 60
        val hour = (time - extra) / 60
        return "${if(hour <= 12) hour else hour - 12}:${String.format(Locale.CANADA, "%02d", extra)} ${if(hour < 12) "am" else "pm"}"
    }

    fun getStrTime(index: Int): String{
        val extra = times[index] % 60
        val hour = (times[index] - extra) / 60
        return "${if(hour <= 12) hour else hour - 12}:${String.format(Locale.CANADA, "%02d", extra)} ${if(hour < 12) "am" else "pm"}"
    }

    //fun getEndDate(): String = endDate
    //fun getStartDate(): String = startDate
    fun getInsertCommand(): String = "Insert Into Clients(name, session_type, days, times, durations, start_date, end_date) Values('$name', ${scheduleType.value}, '${getDaysString(true)}', '${getTimesString(true)}', '${getDurationsString()}', '$startDate', '$endDate')"
    fun getUpdateCommand(): String = "Update Clients Set name = '$name', session_type = ${scheduleType.value}, days = '${getDaysString(true)}', times = '${getTimesString(true)}', durations = '${getDurationsString()}', start_date = '$startDate', end_date = '$endDate' Where id = $id"
    fun getDeleteCommand():String = "Delete From Clients Where id = $id; Delete From Session_Changes Where id = $id; Delete From Session_log Where id = $id"
}