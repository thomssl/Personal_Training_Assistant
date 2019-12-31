package com.trainingapp.personaltrainingassistant

import com.trainingapp.personaltrainingassistant.enumerators.ExerciseType
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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
        fun getDate(strDate: String) : Calendar{
            val cal = Calendar.getInstance()
            if (strDate.length > 10) {
                val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CANADA)
                cal.time = dateFormatter.parse(strDate)
            } else if (strDate != "0") {
                val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.CANADA)
                cal.time = dateFormatter.parse(strDate)
            }
            return cal
        }

        fun getStrDate(date: Calendar): String = SimpleDateFormat("yyyy-MM-dd", Locale.CANADA).format(date.time)
        fun getStrDateTime(date: Calendar): String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CANADA).format(date.time)
        fun getStrDateTimeAMPM(date: Calendar): String = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.CANADA).format(date.time)
        fun getStrTime(date: Calendar): String = SimpleDateFormat("hh:mm", Locale.CANADA).format(date.time)
        fun getStrTimeAMPM(date: Calendar, duration: Int = 0): String {
             return if (duration > 0){
                 val cal = Calendar.getInstance()
                 cal.time = date.time
                 cal[Calendar.MINUTE] = cal[Calendar.MINUTE] + duration
                 "${SimpleDateFormat("hh:mm a", Locale.CANADA).format(date.time)} - ${SimpleDateFormat("hh:mm a", Locale.CANADA).format(cal.time)}"
            } else
                SimpleDateFormat("hh:mm a", Locale.CANADA).format(date.time)
        }

        fun getTimeInt(strTime: String): Int{
            val calendar = Calendar.getInstance()
            calendar.time = SimpleDateFormat("hh:mm a", Locale.CANADA).parse(strTime)
            return (calendar[Calendar.HOUR_OF_DAY]*60) + calendar[Calendar.MINUTE]
        }

        fun getExerciseTypeNameArray(): Array<String>{
            val types = ExerciseType.values()
            return Array(types.size) {i -> types[i].text}
        }

        fun toArrayListInt(str: String): ArrayList<Int>{
            val lstInt = ArrayList<Int>()
            if (str.isNotEmpty()) {
                val lstStr = str.split(",")
                for (entry in lstStr)
                    lstInt.add(entry.toInt())
            }
            return lstInt
        }

        /**
         * formats a checked sql to account for any apostrophes
         */
        fun formatForSQL(strSQL: String): String{
            var temp = strSQL
            if (strSQL.contains("'"))
                temp = strSQL.replace("'", "''")
            return temp
        }

        /**
         * checks user input as String for illegal characters or an empty string
         * @return true if a bad character is found or if an empty string is found
         */
        fun badSQLText(strSQL: String): Boolean{
            if (strSQL.isBlank())
                return true
            for (character in strSQL.toCharArray())
                if (character == ',' || character == ';' || character == '"' || character == '_') return true
            return false
        }

        fun compareTimeRanges(range1: IntRange, range2: IntRange): Boolean{
            var result = false
            range1.forEach{ result = it in range2 || result }
            return result
        }
    }
}