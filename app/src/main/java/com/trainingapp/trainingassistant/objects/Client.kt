package com.trainingapp.trainingassistant.objects

import android.database.Cursor
import com.trainingapp.trainingassistant.database.DBInfo
import com.trainingapp.trainingassistant.enumerators.ScheduleType

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
class Client (
    val id: Int,
    val name: String,
    private val schedule: Schedule,
    val startDate: String,
    val endDate: String
) {
    companion object{
        val empty = Client(
            0,
            "",
            Schedule(
                ScheduleType.NO_SCHEDULE,
                0,
                0,
                mutableListOf(),
                mutableListOf()
            ),
            "",
            ""
        )

        /**
         * Private method to get the ScheduleType enum from an int obtained from the database
         * @param type Int representation of the ScheduleType
         * @return corresponding ScheduleType of the Int parameter
         */
        fun getScheduleType(type: Int): ScheduleType{
            return when (type){
                0 -> ScheduleType.NO_SCHEDULE
                1 -> ScheduleType.WEEKLY_CONSTANT
                2 -> ScheduleType.WEEKLY_VARIABLE
                3 -> ScheduleType.MONTHLY_VARIABLE
                else -> ScheduleType.NO_SCHEDULE
            }
        }

        fun withCursor(it: Cursor): Client {
            return Client(
                it.getInt(it.getColumnIndex(DBInfo.ClientsTable.ID)),
                it.getString(it.getColumnIndex(DBInfo.ClientsTable.NAME)),
                Schedule(
                    getScheduleType(it.getInt(it.getColumnIndex(DBInfo.ClientsTable.SCHEDULE_TYPE))),
                    it.getInt(it.getColumnIndex(DBInfo.ClientsTable.DAYS)),
                    it.getInt(it.getColumnIndex(DBInfo.ClientsTable.DURATION)),
                    mutableListOf(
                        it.getInt(it.getColumnIndex(DBInfo.ClientsTable.SUN)),
                        it.getInt(it.getColumnIndex(DBInfo.ClientsTable.MON)),
                        it.getInt(it.getColumnIndex(DBInfo.ClientsTable.TUE)),
                        it.getInt(it.getColumnIndex(DBInfo.ClientsTable.WED)),
                        it.getInt(it.getColumnIndex(DBInfo.ClientsTable.THU)),
                        it.getInt(it.getColumnIndex(DBInfo.ClientsTable.FRI)),
                        it.getInt(it.getColumnIndex(DBInfo.ClientsTable.SAT))
                    ),
                    mutableListOf(
                        it.getInt(it.getColumnIndex(DBInfo.ClientsTable.SUN_DURATION)),
                        it.getInt(it.getColumnIndex(DBInfo.ClientsTable.MON_DURATION)),
                        it.getInt(it.getColumnIndex(DBInfo.ClientsTable.TUE_DURATION)),
                        it.getInt(it.getColumnIndex(DBInfo.ClientsTable.WED_DURATION)),
                        it.getInt(it.getColumnIndex(DBInfo.ClientsTable.THU_DURATION)),
                        it.getInt(it.getColumnIndex(DBInfo.ClientsTable.FRI_DURATION)),
                        it.getInt(it.getColumnIndex(DBInfo.ClientsTable.SAT_DURATION))
                    )
                ),
                it.getString(it.getColumnIndex(DBInfo.ClientsTable.START_DATE)),
                it.getString(it.getColumnIndex(DBInfo.ClientsTable.END_DATE))
            )
        }
    }

    fun getDuration(dateTime: String): Int = schedule.getDuration(dateTime)
    val strDays: String
        get() = schedule.daysOutput
    val strTimes: String
        get() = schedule.timesOutput
    val strScheduleType: String
        get() = schedule.scheduleType.text
    val scheduleType: ScheduleType
        get() = schedule.scheduleType
    val duration: String
        get() = schedule.duration.toString()
    val days: String
        get() = schedule.days.toString()
    val conflictDays: String
        get() = schedule.checkClientConflictDays
    val sessionDays: List<ClientConflictData>
        get() = schedule.sessionDays
    val daysInfo: List<ClientDaysInfo>
        get() = schedule.daysList.mapIndexedNotNull { index, it ->
            if (it > 0)
                ClientDaysInfo(index, schedule.getStrTime(index), schedule.durationsList[index].toString())
            else
                null
        }

    //Database operations
    val insertCommand: String
        get() =
            """
            Insert Into Clients(client_name, start_date, end_date, schedule_type, days, duration,
                                sun, mon, tue, wed, thu, fri, sat
                                sun_duration, mon_duration, tue_duration, wed_duration, thu_duration, fri_duration, sat_duration)
            Values('$name', '$startDate', '$endDate',${schedule.insertCommand});""".trimIndent()
    val updateCommand: String
        get() =
            """
            Update Clients
            Set    client_name='$name',
                   start_date='$startDate',
                   end_date='$endDate',
                   ${schedule.updateCommand}
            Where  client_id=$id;""".trimIndent()
    val deleteCommand: String
        get() =
            """
            Delete From Clients         Where client_id = $id;
            Delete From Session_Changes Where client_id = $id;
            Delete From Session_log     Where client_id = $id;""".trimIndent()
}