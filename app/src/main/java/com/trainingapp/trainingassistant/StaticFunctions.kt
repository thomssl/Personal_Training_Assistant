package com.trainingapp.trainingassistant

import com.trainingapp.trainingassistant.enumerators.ExerciseType
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Static class to provide other classes and methods with the ability to perform operations common to multiple classes
 * Also provides ability to convert day of the week as a number to the String representation
 */
class StaticFunctions {
    companion object{
        val NumToDay = ArrayList<String>()
        init{
            NumToDay.add("Empty")
            NumToDay.add("Sun")
            NumToDay.add("Mon")
            NumToDay.add("Tue")
            NumToDay.add("Wed")
            NumToDay.add("Thu")
            NumToDay.add("Fri")
            NumToDay.add("Sat")
        }

        /**
         * Method to return a calendar object representing a datetime formatted String
         * @param strDate dateTime formatted String
         * @return Calendar object representing dateTime String sent to object
         */
        fun getDate(strDate: String) : Calendar{
            val cal = Calendar.getInstance()
            val dateFormatter: SimpleDateFormat = if (strDate.length > 10)//if String passed to function contains the time
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CANADA)
            else//if String passed to function does not contain the time
                SimpleDateFormat("yyyy-MM-dd", Locale.CANADA)
            cal.time = dateFormatter.parse(strDate)
            return cal
        }

        //date/time format format functions. Takes input and formats to desired date/time format
        fun getStrDate(date: Calendar): String = SimpleDateFormat("yyyy-MM-dd", Locale.CANADA).format(date.time)
        fun getStrDateTime(date: Calendar): String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CANADA).format(date.time)
        fun getStrDateTimeAMPM(date: Calendar): String = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.CANADA).format(date.time)
        fun getStrTime(date: Calendar): String = SimpleDateFormat("hh:mm", Locale.CANADA).format(date.time)
        fun getStrTimeAMPM(date: Calendar, duration: Int = 0): String {
             return if (duration > 0){//if no duration has been specified, format as 'time am/pm -(time+duration) am/pm'
                 val cal = Calendar.getInstance()
                 cal.time = date.time
                 cal[Calendar.MINUTE] = cal[Calendar.MINUTE] + duration
                 "${SimpleDateFormat("hh:mm a", Locale.CANADA).format(date.time)} - ${SimpleDateFormat("hh:mm a", Locale.CANADA).format(cal.time)}"
            } else//if optional parameter not set, return as 'time am/pm'
                SimpleDateFormat("hh:mm a", Locale.CANADA).format(date.time)
        }

        /**
         * Method to get time as minutes in a day from a time String
         */
        fun getTimeInt(strTime: String): Int{
            val calendar = Calendar.getInstance()
            calendar.time = SimpleDateFormat("hh:mm a", Locale.CANADA).parse(strTime)
            return (calendar[Calendar.HOUR_OF_DAY]*60) + calendar[Calendar.MINUTE]
        }

        /**
         * Method to return list of ExerciseType names. Skips 'BLANK' ExerciseType
         */
        fun getExerciseTypeNameArray(): Array<String>{
            val types = ExerciseType.values()
            return Array(types.size-1) { i -> types[i].text }
        }

        /**
         * Method to convert csv string to ArrayList of Int
         */
        fun toArrayListInt(str: String): ArrayList<Int>{
            val lstInt = ArrayList<Int>()//empty list
            //if not empty string, proceed to separate by ','. if empty string, return empty list
            if (str.isNotEmpty()) {
                val lstStr = str.split(",")
                for (entry in lstStr)
                    lstInt.add(entry.toInt())
            }
            return lstInt
        }

        /**
         * Not Needed
         * Method to format a checked sql to account for any apostrophes
         * @param strSQL String to be used in database operation
         * @return String formatted to be acceptable for database operation
         */
        fun formatForSQL(strSQL: String): String{
            var temp = strSQL
            if (strSQL.contains("'"))
                temp = strSQL.replace("'", "''")
            return temp
        }

        /**
         * Method to check user input as String for illegal characters or an empty string
         * @param strSQL String to be used in database operation
         * @return true if a bad character is found or if an empty string is found
         */
        fun badSQLText(strSQL: String): Boolean{
            if (strSQL.isBlank())
                return true
            for (character in strSQL.toCharArray())
                if (character == ',' || character == ';' || character == '"' || character == '_' || character == '\'') return true
            return false
        }

        /**
         * Method to compare two different ranges to see if there is overlap
         * @return true if overlap found, false if no overlap found
         */
        fun compareTimeRanges(range1: IntRange, range2: IntRange): Boolean{
            //for each value in range 1, check if it can be found in range 2. If found, return true
            for (num in range1){
                if (num in range2)
                    return true
            }
            return false//if no overlap found within loop, return false
        }
    }
}