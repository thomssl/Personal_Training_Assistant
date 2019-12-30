package com.trainingapp.personaltrainingassistant.database

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.trainingapp.personaltrainingassistant.StaticFunctions
import com.trainingapp.personaltrainingassistant.enumerators.ExerciseType
import com.trainingapp.personaltrainingassistant.enumerators.ScheduleType
import com.trainingapp.personaltrainingassistant.objects.*
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class DatabaseOperations(context: Context) {

    companion object {
        private const val INSERT_COMMAND = 1
        private const val UPDATE_COMMAND = 2
        private const val DELETE_COMMAND = 3
        private const val CLIENT_ID = 0
        private const val CLIENT_NAME = 1
        private const val CLIENT_SESSIONS_TYPE = 2
        private const val CLIENT_DAYS = 3
        private const val CLIENT_TIMES = 4
        private const val CLIENT_DURATIONS = 5
        private const val CLIENT_START_DATE = 6
        private const val CLIENT_END_DATE = 7
        private const val EXERCISE_ID = 0
        private const val EXERCISE_NAME = 1
        private const val EXERCISE_TYPE = 2
        private const val EXERCISE_PRIMARY_MOVER = 3
        private const val EXERCISE_SECONDARY_MOVERS = 4
        private const val SESSION_CLIENT_ID = 0
        private const val SESSION_DAYTIME = 1
        private const val SESSION_EXERCISE_IDS = 2
        private const val SESSION_SETS = 3
        private const val SESSION_REPS = 4
        private const val SESSION_RESISTANCES = 5
        private const val SESSION_EXERCISE_ORDERS = 6
        private const val SESSION_NOTES = 7
        private const val SESSION_DURATION = 8
        private const val SETTINGS_DEFAULT_DURATIONS = 0
        private const val SETTINGS_24_CLOCK = 1
    }

    private var databaseHelper = DatabaseHelper(context)
    private var db: SQLiteDatabase

    init {
        db = databaseHelper.writableDatabase
    }

    /**
     * Method attempts SQL Command that does not require a cursor for results and returns true if no SQLException has occurred
     * @param sql SQL insert, update or delete command
     * @return true if no exception occurs during the SQL command
     */
    private fun trySQLCommand(sql: String): Boolean {
        return try {
            db.execSQL(sql)
            true
        } catch (e:SQLException){
            e.printStackTrace()
            false
        }
    }

    /**
     * Method to convert csv values for exercise_ids, sets, reps, resistances and exercise_orders into an ArrayList of ExerciseSession Objects
     * @param ids exercise_ids as csv
     * @param sets sets as csv
     * @param reps reps as csv
     * @param resistances resistances as csv
     * @return ArrayList of ExerciseSession objects populated with the given values
     */
    private fun getListExerciseSessions(ids: String, sets: String, reps: String, resistances: String, orders: String): ArrayList<ExerciseSession>{
        val exercises = ArrayList<ExerciseSession>()
        if (ids.isEmpty())
            return exercises
        val lstIDs = StaticFunctions.toArrayListInt(ids)
        val lstSets = sets.split(",")
        val lstReps = reps.split(",")
        val lstResistances = resistances.split(",")
        val lstOrders = StaticFunctions.toArrayListInt(orders)

        for(i in lstIDs.indices)
            exercises.add(
                ExerciseSession(
                    getExercise(lstIDs[i]),
                    lstSets[i],
                    lstReps[i],
                    lstResistances[i],
                    lstOrders[i]
                )
            )
        return exercises
    }

    private fun getScheduleType(type: Int): ScheduleType{
        return when (type){
            0 -> ScheduleType.NO_SCHEDULE
            1 -> ScheduleType.WEEKLY_CONSTANT
            2 -> ScheduleType.WEEKLY_VARIABLE
            3 -> ScheduleType.MONTHLY_VARIABLE
            else -> ScheduleType.NO_SCHEDULE
        }
    }

    private fun addSecondaryMoversNames(exercise: Exercise): Exercise{
        if (exercise.id == 0 || exercise.strSecondaryMovers == "0"){
            exercise.strSecondaryMoversList.add("")
            return exercise
        }
        val tableName = if (exercise.type == ExerciseType.STRENGTH) "Muscles" else "Joints"
        val cursor = db.rawQuery("Select name From $tableName Where id in (${exercise.strSecondaryMovers})", null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                exercise.strSecondaryMoversList.add(cursor.getString(0))
                cursor.moveToNext()
            }
        }
        cursor.close()
        return exercise
    }

    fun getUserSettings(): ArrayList<Int>{
        val cursor = db.rawQuery("Select default_duration, '24_clock' From User_Settings", null)
        val result = if (cursor.moveToFirst()) ArrayList<Int>(arrayOf(cursor.getInt(SETTINGS_DEFAULT_DURATIONS), cursor.getInt(SETTINGS_24_CLOCK)).asList()) else ArrayList()
        cursor.close()
        return result
    }

    fun setUserSettings(settings: ArrayList<Int>): Boolean = trySQLCommand("Update User_Settings Set default_duration = ${settings[0]}, '24_clock' = ${settings[1]}")

    /**
     * Method to get the name of joint given the joint id. See Joints Table for more information
     * @param id id of the joint as found in the Joints Table
     * @return joint name corresponding with the given id
     */
    fun getJoint(id: Int): MuscleJoint {
        val cursor = db.rawQuery("Select id, name From Joints Where id = $id", null)
        val result = if (cursor.moveToFirst()) MuscleJoint(
            cursor.getInt(0),
            cursor.getString(1)
        ) else MuscleJoint(
            0,
            ""
        )
        cursor.close()
        return result
    }

    fun getAllJoints(): ArrayList<MuscleJoint>{
        val joints = ArrayList<MuscleJoint>()
        val cursor = db.rawQuery("Select id, name From Joints", null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                joints.add(
                    MuscleJoint(
                        cursor.getInt(0),
                        cursor.getString(1)
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        return joints
    }

    /** DEPRECATED
     * Method to get the name of the mover muscle from the muscle id. See Muscles Table for more information
     * @param id id of the muscle as found in the Muscles Table
     * @return muscle name corresponding with the given id
     */
    fun getMoverName(id: Int, exerciseType: Int): String{
        if (id == 0)
            return ""
        return when (exerciseType){
            1 -> {
                val cursor = db.rawQuery("Select name From Muscles Where id = $id", null)
                var result = ""
                if (cursor.moveToFirst())
                    result = cursor.getString(0)
                cursor.close()
                result
            }
            2 -> {
                val cursor = db.rawQuery("Select name From Joints Where id = $id", null)
                var result = ""
                if (cursor.moveToFirst())
                    result = cursor.getString(0)
                cursor.close()
                result
            }
            else -> "error"
        }
    }

    fun getMuscle(id: Int): MuscleJoint {
        val cursor = db.rawQuery("Select id, name From Muscles Where id = $id", null)
        val muscle = if (cursor.moveToFirst()) MuscleJoint(
            cursor.getInt(0),
            cursor.getString(1)
        ) else MuscleJoint(
            -1,
            ""
        )
        cursor.close()
        return muscle
    }

    fun getAllMuscles(): ArrayList<MuscleJoint>{
        val muscles = ArrayList<MuscleJoint>()
        val cursor = db.rawQuery("Select id, name From Muscles Order By name", null)
        if (cursor.moveToFirst()) {
            while(!cursor.isAfterLast) {
                muscles.add(
                    MuscleJoint(
                        cursor.getInt(0),
                        cursor.getString(1)
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        return muscles
    }

    /**
     * Method used to remove a muscle from the database. Will not remove a muscle if the muscle is used to define an exercise (ie defines the primary_mover or is a secondary_mover)
     * @param muscleJoint muscle data class containing the id and name of the muscle to be removed
     * @return if the muscle defines an exercise 2, if the deletion is handled with no error 1, if the deletion has an error 0
     */
    fun removeMuscle(muscleJoint: MuscleJoint): Int{
        val cursor = db.rawQuery("Select * From Exercises Where primary_mover = ${muscleJoint.id} Or secondary_movers Like '${muscleJoint.id},%' Or secondary_movers Like '%,${muscleJoint.id},%' Or secondary_movers Like '%,${muscleJoint.id}' or secondary_movers = '${muscleJoint.id}'", null)
        val result =  if (cursor.moveToFirst())
            2
        else {
            if (deleteMuscle(muscleJoint)) 1 else 0
        }
        cursor.close()
        return result
    }

    fun addMuscle(muscleJoint: MuscleJoint): Boolean = trySQLCommand("Insert Into Muscles(name) Values('${muscleJoint.name}')")
    fun updateMuscle(muscleJoint: MuscleJoint): Boolean = trySQLCommand("Update Muscles Set name = '${muscleJoint.name}' Where id = ${muscleJoint.id}")
    private fun deleteMuscle(muscleJoint: MuscleJoint): Boolean = trySQLCommand("Delete From Muscles Where id = ${muscleJoint.id}")

    /** DEPRECATED
     * Method takes the result of ExerciseSession.getSecondaryMoversString() or Exercise.getSecondaryMoversString() and returns a csv string representing the names of the secondary movers
     * @param strMovers csv text representing the ids of the secondary movers within an exercise
     * @return csv text with the names of each id given in the parameter text
     */
    fun getSecondaryMoversNames(strMovers: String): String{
        val cursor = db.rawQuery("Select name From Muscles Where id in ($strMovers)", null)
        val builder = StringBuilder()
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast){
                builder.append(cursor.getString(0))
                builder.append(',')
            }
            builder.deleteCharAt(builder.lastIndex)
        }
        cursor.close()
        return builder.toString()
    }

    /**
     * Method to get a Client object given a client id by searching for that client's information. If a client is not found with the given id, a blank client is returned
     * @param id id of the client to be found
     * @return Client object for the given client id
     */
    fun getClient(id: Int): Client {
        val cursor = db.rawQuery("Select * From Clients Where id = $id", null)
        val client: Client = if (cursor.moveToFirst())
            Client(
                cursor.getInt(CLIENT_ID),
                cursor.getString(CLIENT_NAME),
                getScheduleType(cursor.getInt(CLIENT_SESSIONS_TYPE)),
                cursor.getString(CLIENT_DAYS),
                cursor.getString(CLIENT_TIMES),
                cursor.getString(CLIENT_DURATIONS),
                cursor.getString(CLIENT_START_DATE),
                cursor.getString(CLIENT_END_DATE)
            )
        else
            Client(
                0,
                "",
                ScheduleType.NO_SCHEDULE,
                "0",
                "0",
                "0",
                "0",
                "0"
            )
        cursor.close()
        return client
    }

    /**
     * Method to get all clients currently in the Clients Table
     * @return ArrayList of Client objects
     */
    fun getAllClients(): ArrayList<Client>{
        val cursor = db.rawQuery("Select * From Clients Order By name", null)
        val clients = ArrayList<Client>()
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                clients.add(
                    Client(
                        cursor.getInt(CLIENT_ID),
                        cursor.getString(CLIENT_NAME),
                        getScheduleType(cursor.getInt(CLIENT_SESSIONS_TYPE)),
                        cursor.getString(CLIENT_DAYS),
                        cursor.getString(CLIENT_TIMES),
                        cursor.getString(CLIENT_DURATIONS),
                        cursor.getString(CLIENT_START_DATE),
                        cursor.getString(CLIENT_END_DATE)
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        return clients
    }

    /**
     * Method to get the client type. 1 = weekly constant schedule, 2 = weekly variable session, 3 = monthly variable schedule
     * @param clientID client id value. See Clients Table
     * @return Int value representing the schedule type that a given client follows
     */
    private fun getClientType(clientID: Int): ScheduleType{
        val cursor = db.rawQuery("Select session_type From Clients Where client_id = $clientID", null)
        val result: ScheduleType = if (cursor.moveToFirst()) getScheduleType(cursor.getInt(0)) else ScheduleType.NO_SCHEDULE
        cursor.close()
        return result
    }

    /**
     * Method to check if a client conflicts with the given client list. Only looks at clients with constant schedules (ie session_type = 1)
     * @param client Client object created from data collected in the CreateOrAlterClientActivity
     * @return empty string if a conflict is found with the current constant schedule. String with conflict client names if any conflicts are found
     */
    fun checkClientConflict(client: Client): String{
        var conflicts = ""
        for(index in client.days.indices) {
            val day = client.days[index]
            val cursor = db.rawQuery("Select times, durations, name, days From Clients Where days Like '%$day%' And start_date <= date('now') And end_date >= date('now') And session_type = 1", null)
            if (cursor.moveToFirst()) {
                while(!cursor.isAfterLast) {
                    val position = cursor.getString(3).split(",").indexOf(day.toString())
                    val time = cursor.getString(0).split(",")[position].toInt()
                    val duration = cursor.getString(1).split(",")[position].toInt()
                    val upper = time + duration
                    if (StaticFunctions.compareTimeRanges(time..upper,client.times[index] until (client.times[index] + client.durations[index])))
                        conflicts += "${cursor.getString(2)},"
                    cursor.moveToNext()
                }
            }
            cursor.close()
        }
        if (conflicts.isNotEmpty())
            return conflicts.substring(0 until conflicts.lastIndex)
        return conflicts
    }


    fun insertClient(client: Client): Boolean = trySQLCommand(client.getInsertCommand())
    fun updateClient(client: Client): Boolean = trySQLCommand(client.getUpdateCommand())
    fun deleteClient(client: Client): Boolean = trySQLCommand(client.getDeleteCommand())

    /**
     * DEPRECATED
     */
    fun checkExerciseType(id: Int): Int{
        val cursor = db.rawQuery("Select e.type From Exercises e Where e.id = $id", null)
        val exerciseType = if (cursor.moveToFirst()) cursor.getInt(0) else -1
        cursor.close()
        return exerciseType
    }

    private fun getExerciseType(value: Int): ExerciseType{
        return when(value){
            1 -> ExerciseType.STRENGTH
            2 -> ExerciseType.MOBILITY
            3 -> ExerciseType.STABILITY
            else -> ExerciseType.BLANK
        }
    }

    /**
     * Method to get an Exercises object given and exercise id
     * @param id id value of an exercise. See Exercises Table for more information
     * @return Exercise object for the given id value
     */
    fun getExercise(id: Int): Exercise {
        val cursor = db.rawQuery("Select e.id, e.name, e.type, e.primary_mover, e.secondary_movers, Case When e.type = 1 then m.name When e.type = 2 or e.type = 3 then j.name End From Exercises e inner join Exercise_Types t on e.type=t.id left join Muscles m on e.primary_mover=m.id left join Joints j on e.primary_mover=j.id Where e.id = $id", null)
        var exercise = if (cursor.moveToFirst()) Exercise(
            cursor.getInt(EXERCISE_ID),
            cursor.getString(EXERCISE_NAME),
            getExerciseType(cursor.getInt(EXERCISE_TYPE)),
            cursor.getInt(EXERCISE_PRIMARY_MOVER),
            cursor.getString(5),
            cursor.getString(EXERCISE_SECONDARY_MOVERS)
        )
        else Exercise(
            0,
            "",
            ExerciseType.BLANK,
            0,
            "",
            ""
        )
        cursor.close()
        exercise = addSecondaryMoversNames(exercise)
        return exercise
    }

    /**
     * Method to get all the exercises with in the Exercises Table
     * @return ArrayList of Exercise objects representing the entire Exercise library
     */
    fun getAllExercises(): ArrayList<Exercise>{
        val cursor = db.rawQuery("Select e.id, e.name, e.type, e.primary_mover, e.secondary_movers, Case When e.type = 1 then m.name When e.type = 2 or e.type = 3 then j.name End From Exercises e inner join Exercise_Types t on e.type=t.id left join Muscles m on e.primary_mover=m.id left join Joints j on e.primary_mover=j.id Order By e.name", null)
        val exercises = ArrayList<Exercise>()
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                exercises.add(
                    Exercise(
                        cursor.getInt(EXERCISE_ID),
                        cursor.getString(EXERCISE_NAME),
                        getExerciseType(cursor.getInt(EXERCISE_TYPE)),
                        cursor.getInt(EXERCISE_PRIMARY_MOVER),
                        cursor.getString(5),
                        cursor.getString(EXERCISE_SECONDARY_MOVERS)
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        for(exercise in exercises){
            exercises[exercises.indexOf(exercise)] = addSecondaryMoversNames(exercise)
        }
        return exercises
    }

    /**
     * Method to check if a current exercise name is already in the Exercises Table
     * @param exercise Exercise object collected from the user in CreateOrAlterExerciseActivity
     * @return true if a conflict is found
     */
    fun checkExerciseConflict(exercise: Exercise): Boolean{
        val cursor = db.rawQuery("Select name From Exercises Where name = '${exercise.name}'", null)
        val result = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun updateExercise(exercise: Exercise): Boolean = trySQLCommand(exercise.getUpdateCommand())
    fun insertExercise(exercise: Exercise): Boolean = trySQLCommand(exercise.getInsertCommand())
    private fun deleteExercise(exercise: Exercise): Boolean = trySQLCommand(exercise.getDeleteCommand())

    fun removeExercise(exercise: Exercise): Boolean {
        val sessions = ArrayList<Session>()
        val cursor = db.rawQuery("Select s.client_id, s.dayTime, s.exercise_ids, s.sets, s.reps, s.resistances, s.exercise_order, s.notes, s.duration, c.name From Session_log s Inner Join Clients c On s.client_id=c.id where (exercise_ids Like '${exercise.id},%' Or exercise_ids Like '%,${exercise.id},%' Or exercise_ids Like '%,${exercise.id}' or exercise_ids = '${exercise.id}')", null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                sessions.add(Session(
                    cursor.getInt(
                        SESSION_CLIENT_ID
                    ),
                    cursor.getString(9),
                    cursor.getString(SESSION_DAYTIME),
                    getListExerciseSessions(
                        cursor.getString(SESSION_EXERCISE_IDS),
                        cursor.getString(SESSION_SETS),
                        cursor.getString(SESSION_REPS),
                        cursor.getString(SESSION_RESISTANCES),
                        cursor.getString(SESSION_EXERCISE_ORDERS)
                    ),
                    cursor.getString(SESSION_NOTES),
                    cursor.getInt(SESSION_DURATION)
                ))
                val exerciseSession = sessions[sessions.lastIndex].getExercise(exercise)
                sessions[sessions.lastIndex].removeExercise(exercise)
                sessions[sessions.lastIndex].notes += "*removed:${exerciseSession.name} sets:${exerciseSession.sets} reps:${exerciseSession.reps} resistance:${exerciseSession.resistance}"
            }
        }
        cursor.close()
        sessions.forEach { updateSession(it, "") }
        return deleteExercise(exercise)
    }

    /**
     * Method to get a Session object given a client id and daytime value (dayTime format "yyyy-MM-dd HH:mm" 24hour). Used when passing to the SessionActivity
     * @param clientID id value of the session holder
     * @param dayTime day and time in a parsable string
     * @return Session object corresponding to the given values
     */
    fun getSession(clientID: Int, dayTime: String): Session {
        val cursor = db.rawQuery("Select s.client_id, s.dayTime, s.exercise_ids, s.sets, s.reps, s.resistances, s.exercise_order, s.notes, s.duration, c.name From Session_log s Inner Join Clients c On s.client_id=c.id Where s.client_id = $clientID And date(dayTime) = date('$dayTime')", null)
        val session: Session = if (cursor.moveToFirst()){
            Session(
                cursor.getInt(SESSION_CLIENT_ID),
                cursor.getString(9),
                cursor.getString(SESSION_DAYTIME),
                getListExerciseSessions(
                    cursor.getString(SESSION_EXERCISE_IDS),
                    cursor.getString(SESSION_SETS),
                    cursor.getString(SESSION_REPS),
                    cursor.getString(SESSION_RESISTANCES),
                    cursor.getString(SESSION_EXERCISE_ORDERS)
                ),
                cursor.getString(SESSION_NOTES),
                cursor.getInt(SESSION_DURATION)
            )
        } else{
            val client = getClient(clientID)
            val session =
                Session(
                    clientID,
                    client.name,
                    dayTime,
                    ArrayList(),
                    "",
                    0
                )
            if (checkChange(session)){
                val cursor1 = db.rawQuery("Select duration From Session_Changes Where client_id = $clientID And datetime(change_dayTime) = datetime('$dayTime')", null)
                cursor1.moveToFirst()
                session.duration = cursor1.getInt(0)
                cursor1.close()
            } else
                session.duration = client.getDuration(dayTime)
            session
        }
        cursor.close()
        return session
    }

    fun removeCanceledSession(clientID: Int): Boolean = trySQLCommand("Delete Top(1) From Session_Changes Where client_id = $clientID")

    fun checkSessionConflict(session: Session, isSameDate: Boolean): Boolean{
        Log.d("here", "DayTime: ${StaticFunctions.getStrDateTime(session.date)}")
        val day = getScheduleByDay(session.date)
        return day.checkConflict(session, isSameDate)
    }

    fun checkSessionLog(session: Session): Boolean{
        val cursor = db.rawQuery("Select client_id From Session_log s Where s.client_id = ${session.clientID} And date(dayTime) = date('${StaticFunctions.getStrDateTime(session.date)}')", null)
        val result = cursor.moveToFirst()
        cursor.close()
        return result
    }

    /**
     * Method to insert a session. Will remove any changes to the date or time associated with that session
     * @param session Session object containing the SQL commands to clear changes and insert into the Session_log
     * @return true if the SQL command is successful
     */
    fun insertSession(session: Session): Boolean {
        deleteChange(session)
        return trySQLCommand(session.getSQLCommand(INSERT_COMMAND))
    }

    fun updateSession(session: Session, oldDayTime: String): Boolean = trySQLCommand(session.getSQLCommand(UPDATE_COMMAND, oldDayTime))
    private fun deleteSession(session: Session): Boolean = trySQLCommand(session.getSQLCommand(DELETE_COMMAND))

    /**
     * Method to cancel a given session. Course of action is dependent on the client's session_type. See Client Table for more information on session_type
     * @param session Session object to be canceled
     * @return true if cancellation was successful
     */
    fun cancelSession(session: Session): Boolean{
        return when(getClientType(session.clientID)){
            ScheduleType.WEEKLY_CONSTANT ->  {
                when (true){
                    checkChange(session) -> updateChange(session, Session(session.clientID, session.clientName, "0", ArrayList(), "", session.duration))
                    checkSessionLog(session) -> deleteSession(session)
                    else -> insertChange(session, Session(session.clientID, session.clientName, "0", ArrayList(), "", session.duration))
                }
            }
            ScheduleType.NO_SCHEDULE,ScheduleType.WEEKLY_VARIABLE,ScheduleType.MONTHLY_VARIABLE -> deleteSession(session)
        }
    }

    /**
     * Method to get all logged sessions belonging to a client
     * @param client Client object representing the client who to be searched for logged sessions
     * @return ArrayList of Session objects representing all the sessions logged for a client
     */
    fun getClientSessions(client: Client): ArrayList<Session>{
        val sessions = ArrayList<Session>()
        val cursor = db.rawQuery("Select s.client_id, s.dayTime, s.exercise_ids, s.sets, s.reps, s.resistances, s.exercise_order, s.notes, s.duration, c.name From Session_log s Inner Join Clients c On s.client_id=c.id Where s.id = ${client.id}", null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                sessions.add(
                    Session(
                        cursor.getInt(SESSION_CLIENT_ID),
                        cursor.getString(9),
                        cursor.getString(SESSION_DAYTIME),
                        getListExerciseSessions(
                            cursor.getString(SESSION_EXERCISE_IDS),
                            cursor.getString(SESSION_SETS),
                            cursor.getString(SESSION_REPS),
                            cursor.getString(SESSION_RESISTANCES),
                            cursor.getString(SESSION_EXERCISE_ORDERS)
                        ),
                        cursor.getString(SESSION_NOTES),
                        cursor.getInt(SESSION_DURATION)
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        return sessions
    }

    fun getAddSessionsClientsByDay(calendar: Calendar): ArrayList<Client>{
        val day = getScheduleByDay(calendar)
        val tempCal = Calendar.getInstance()
        tempCal.time = calendar.time
        val month = tempCal[Calendar.MONTH]
        val year = tempCal[Calendar.YEAR]
        tempCal[Calendar.DAY_OF_WEEK] = 1
        val startWeek  = StaticFunctions.getStrDate(tempCal)
        tempCal[Calendar.DAY_OF_WEEK] = 7
        val endWeek = StaticFunctions.getStrDate(tempCal)
        tempCal[Calendar.MONTH] = month
        tempCal[Calendar.YEAR] = year
        tempCal[Calendar.DAY_OF_MONTH] = 1
        val startMonth = StaticFunctions.getStrDate(tempCal)
        tempCal[Calendar.DAY_OF_MONTH] = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val endMonth = StaticFunctions.getStrDate(tempCal)
        val clients = ArrayList<Client>()
        val cursor = db.rawQuery("Select * From Clients c Where c.id Not in (${day.getStrIDs()}) And (c.session_type = 0 Or (c.session_type = 1 And (Select sc.client_id From Session_Changes sc Where sc.client_id = c.id And sc.change_dayTime = '0') > 0) Or (c.session_type = 2 And (Select count(sl.client_id) From Session_log sl Where sl.client_id = c.id And date(sl.dayTime) >= date('${startWeek}') And date(sl.dayTime) <= date('${endWeek}')) < c.days) Or (c.session_type = 3 And (Select count(sl.client_id) From Session_log sl Where sl.client_id = c.id And date(sl.dayTime) >= date('${startMonth}') And date(sl.dayTime) <= date('${endMonth}')) < c.days))", null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                clients.add(Client(
                    cursor.getInt(CLIENT_ID),
                    cursor.getString(CLIENT_NAME),
                    getScheduleType(cursor.getInt(CLIENT_SESSIONS_TYPE)),
                    cursor.getString(CLIENT_DAYS),
                    cursor.getString(CLIENT_TIMES),
                    cursor.getString(CLIENT_DURATIONS),
                    cursor.getString(CLIENT_START_DATE),
                    cursor.getString(CLIENT_END_DATE)
                ))
                cursor.moveToNext()
            }
        }
        cursor.close()
        return clients
    }

    /**
     * Method to get all the sessions corresponding to a given date. Hierarchy of data is Session Log, Normal Schedule from client info (excluding clients who already have sessions logged or if changes have been made to the schedule)
     * then the session changes made (excluding clients who already have sessions logged). If the sessions are not obtained from Session_log, ExerciseSession's ArrayList and notes String are left empty
     * @param calendar Calendar object representing a date to find the corresponding sessions
     * @return Day object containing the ArrayList of session objects obtained from session queries
     */
    fun getScheduleByDay(calendar: Calendar): Day {
        val sessions = ArrayList<Session>()
        val calendarNow = Calendar.getInstance()
        val calendarChosen = Calendar.getInstance()
        calendarChosen.time = calendar.time
        val currentDate = (calendarNow[Calendar.YEAR] * 365) + calendarNow[Calendar.DAY_OF_YEAR]
        val chosenDate = (calendarChosen[Calendar.YEAR] * 365) + calendarChosen[Calendar.DAY_OF_YEAR]

        val date =
            StaticFunctions.getStrDateTime(
                calendarChosen
            )
        var cursor = db.rawQuery("Select s.client_id, s.dayTime, s.exercise_ids, s.sets, s.reps, s.resistances, s.exercise_order, s.notes, s.duration, c.name From Session_log s Inner Join Clients c On s.client_id=c.id Where date(dayTime) = date('$date')", null)
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast){
                sessions.add(
                    Session(
                        cursor.getInt(SESSION_CLIENT_ID),
                        cursor.getString(9),
                        cursor.getString(SESSION_DAYTIME),
                        getListExerciseSessions(
                            cursor.getString(SESSION_EXERCISE_IDS),
                            cursor.getString(SESSION_SETS),
                            cursor.getString(SESSION_REPS),
                            cursor.getString(SESSION_RESISTANCES),
                            cursor.getString(SESSION_EXERCISE_ORDERS)
                        ),
                        cursor.getString(SESSION_NOTES),
                        cursor.getInt(SESSION_DURATION)
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        return if (chosenDate < currentDate) Day(sessions)
        else {
            val dayOfWeek = calendarChosen[Calendar.DAY_OF_WEEK]
            cursor = db.rawQuery("Select c.id, c.name, c.times, c.days, c.durations From Clients c Where days Like '%$dayOfWeek%' And date(end_date) >= date('${StaticFunctions.getStrDateTime(calendarChosen)}') And date(start_date) <= date('${StaticFunctions.getStrDateTime(calendarChosen)}') And session_type = 1 And Not (c.id in (Select Distinct client_id From Session_Changes Where date(normal_dayTime) = date('$date')) Or c.id in (Select s.client_id From Session_log s Where date(dayTime) = date('$date')))", null)
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast){
                    val index = cursor.getString(3).split(",").indexOf(dayOfWeek.toString())
                    val time = StaticFunctions.toArrayListInt(cursor.getString(2))[index]
                    val duration = StaticFunctions.toArrayListInt(cursor.getString(4))[index]
                    val minutes = time % 60
                    val hour = (time - minutes) / 60
                    calendarChosen[Calendar.HOUR_OF_DAY] = hour
                    calendarChosen[Calendar.MINUTE] = minutes
                    sessions.add(
                        Session(
                            cursor.getInt(0),
                            cursor.getString(1),
                            StaticFunctions.getStrDateTime(
                                calendarChosen
                            ),
                            ArrayList(),
                            "",
                            duration
                        )
                    )
                    cursor.moveToNext()
                }
            }
            cursor.close()
            cursor = db.rawQuery("Select sc.client_id, c.name, sc.change_dayTime, sc.duration From Session_Changes sc Inner Join Clients c On c.id=sc.client_id where date(sc.change_dayTime) = date('$date')  And Not sc.client_id in (Select s.client_id From Session_log s Where date(dayTime) = date('$date'))", null)
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast){
                    sessions.add(
                        Session(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            ArrayList(),
                            "",
                            cursor.getInt(3)
                        )
                    )
                    cursor.moveToNext()
                }
            }
            cursor.close()

            Day(sessions)
        }
    }

    /**
     * Method to check if a session exists in the Session_Changes Table
     * @param session Session object containing the information about a session
     * @return true if session exists
     */
    fun checkChange(session: Session): Boolean{
        val cursor = db.rawQuery("Select client_id From Session_Changes Where client_id = ${session.clientID} And datetime(change_dayTime) = datetime('${StaticFunctions.getStrDateTime(session.date)}')", null)
        val result: Boolean = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun insertChange(oldSession: Session, newSession: Session): Boolean = trySQLCommand("Insert Into Session_Changes(client_id, normal_dayTime, change_dayTime, duration) Values(${oldSession.clientID}, '${StaticFunctions.getStrDateTime(oldSession.date)}', '${StaticFunctions.getStrDateTime(newSession.date)}', ${newSession.duration})")
    fun updateChange(oldSession: Session, newSession: Session): Boolean = trySQLCommand("Update Session_Changes Set change_dayTime = '${StaticFunctions.getStrDateTime(newSession.date)}', duration = ${newSession.duration} Where client_id = ${oldSession.clientID} And datetime(change_dayTime) = datetime('${StaticFunctions.getStrDateTime(oldSession.date)}')")
    private fun deleteChange(session: Session): Boolean = trySQLCommand("Delete From Session_Changes Where client_id = ${session.clientID} And datetime(change_dayTime) = datetime('${StaticFunctions.getStrDateTime(session.date)}')")

    /**
     * Method to get the most recent values of an exercise that was completed by a given client
     * @param exercise Exercise object used to get the exercise id and to populate the ExerciseSession object
     * @param clientID id of the given client used to search Session_log table
     * @return ExerciseSession object containing the information about the last occurrence of the given exercise. All non-Exercise object fields are blank if the client has not already performed the exercise
     */
    fun getLastOccurrence(exercise: Exercise, clientID: Int): ExerciseSession {
        val cursor = db.rawQuery("Select sets, reps, resistances, exercise_ids, exercise_order From Session_log where client_id = $clientID And (exercise_ids Like '${exercise.id},%' Or exercise_ids Like '%,${exercise.id},%' Or exercise_ids Like '%,${exercise.id}' or exercise_ids = '${exercise.id}') Order By dayTime Desc", null)
        val exerciseSession: ExerciseSession = if (cursor.moveToFirst()){
            val index = cursor.getString(3).split(",").indexOf(exercise.id.toString())
            ExerciseSession(
                exercise,
                cursor.getString(0).split(",")[index],
                cursor.getString(1).split(",")[index],
                cursor.getString(2).split(",")[index],
                cursor.getString(3).split(",")[index].toInt()
            )
        } else
            ExerciseSession(
                exercise,
                "",
                "",
                "",
                0
            )
        cursor.close()
        return exerciseSession
    }

    /**
     * Method to get all the occurrences of a given exercise with a given client. Used for progress tracking
     * @param exercise Exercise object used to get the exercise id and to populate the ExerciseSession object
     * @param clientID id of the given client used to search Session_log table
     * @return ExerciseSession object containing the information about the all the occurrences of the given exercise. Empty ArrayList returned if the given exercise has been performed
     */
    fun getAllOccurrences(exercise: Exercise, clientID: Int): ArrayList<ExerciseSession>{
        val exerciseSessions = ArrayList<ExerciseSession>()
        val cursor = db.rawQuery("Select sets, reps, resistances, exercise_ids From Session_log where client_id = $clientID And (exercise_ids Like '${exercise.id},%' Or exercise_ids Like '%,${exercise.id},%' Or exercise_ids Like '%,${exercise.id}' or exercise_ids = '${exercise.id}') Order By dayTime", null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                val index = cursor.getString(3).split(",").indexOf(exercise.id.toString())
                exerciseSessions.add(
                    ExerciseSession(
                        exercise,
                        cursor.getString(0).split(",")[index],
                        cursor.getString(1).split(",")[index],
                        cursor.getString(2).split(",")[index],
                        0
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        return exerciseSessions
    }
}