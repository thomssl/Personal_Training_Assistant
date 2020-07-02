package com.trainingapp.trainingassistant.database

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.trainingapp.trainingassistant.StaticFunctions
import com.trainingapp.trainingassistant.enumerators.ScheduleType
import com.trainingapp.trainingassistant.objects.*
import java.util.*

class DatabaseOperations(val context: Context) {

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
        var result = true
        db.beginTransaction()
        try {
            db.execSQL(sql)
            db.setTransactionSuccessful()
        } catch (e:SQLException){
            e.printStackTrace()
            result = false
        } finally {
            db.endTransaction()
        }

        return result
    }

    /**
     * Method attempts list of SQL Commands that does not require a cursor for results and returns true if no SQLException has occurred
     * @param sql list of SQL insert, update or delete commands
     * @return true if no exception occurs during the SQL commands
     */
    private fun trySQLCommands(sql: List<String>): Boolean {
        var result = true
        db.beginTransaction()
        try {
            sql.forEach { db.execSQL(it) }
            db.setTransactionSuccessful()
        } catch (e:SQLException){
            e.printStackTrace()
            result = false
        } finally {
            db.endTransaction()
        }

        return result
    }

    private fun getSecondaryMoversFromCSV(id: Int, csvSecondaryMoversIDs: String, csvSecondaryMoversNames: String): MutableList<MuscleJoint> {
        // if the id = 0 that means no exercise was found. If strSecondaryMovers is empty than no secondary movers are present
        // Either way, return a blank list is returned
        if (id == 0 || csvSecondaryMoversIDs == "0")
            return mutableListOf()
        val lstSecondaryMoversIDs = StaticFunctions.toListInt(csvSecondaryMoversIDs)
        val lstSecondaryMoversNames = csvSecondaryMoversNames.split(",")
        return lstSecondaryMoversIDs
            .mapIndexed { index, s -> MuscleJoint(s, lstSecondaryMoversNames[index]) }
            .toMutableList()
    }

    /**
     * Needs to be updated for multiple users?
     * Method to get the user settings from the database
     * @return List of the user settings as Ints
     */
    fun getUserSettings(): List<Int>{
        val cursor = db.rawQuery(DBQueries.DBOperations.getUserSettings(), null)
        val result = if (cursor.moveToFirst())
            listOf(
                cursor.getInt(cursor.getColumnIndex(DBInfo.UserSettingsTable.DEFAULT_DURATION)),
                cursor.getInt(cursor.getColumnIndex(DBInfo.UserSettingsTable.CLOCK_24))
            )
        else
            listOf()
        cursor.close()
        return result
    }

    fun setUserSettings(settings: MutableList<Int>): Boolean = trySQLCommand(DBQueries.DBOperations.setUserSettings(settings[0],settings[1]))

    /**
     * Method to get the name of joint given the joint id. See Joints Table for more information
     * @param id id of the joint as found in the Joints Table
     * @return joint name corresponding with the given id
     */
    fun getJoint(id: Int): MuscleJoint {
        val cursor = db.rawQuery(DBQueries.DBOperations.getJoint(id), null)
        val result = if (cursor.moveToFirst())
            MuscleJoint.withCursor(cursor, isMuscle = false)
        else
            MuscleJoint.empty
        cursor.close()
        return result
    }

    /**
     * Method to get all the joints found in the database
     * @return MutableList of MuscleJoint data objects for all the joints defined
     */
    fun getAllJoints(): MutableList<MuscleJoint>{
        val cursor = db.rawQuery(DBQueries.DBOperations.getAllJoints(), null)
        val joints = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map { MuscleJoint.withCursor(it, isMuscle = false) }
            .toMutableList()
        cursor.close()
        return joints
    }

    /**
     * Method to get a single muscle from the Muscles Table based upon a given id
     * @param id id of the muscle to be found
     * @return MuscleJoint data object representing the desired muscle
     */
    fun getMuscle(id: Int): MuscleJoint {
        val cursor = db.rawQuery(DBQueries.DBOperations.getMuscle(id), null)
        val muscle = if (cursor.moveToFirst())
            MuscleJoint.withCursor(cursor, isMuscle = true)
        else
            MuscleJoint.empty
        cursor.close()
        return muscle
    }

    /**
     * Method to get all the muscles from the Muscles Table
     * @return MutableList of MuscleJoint data objects representing all the defined muscles
     */
    fun getAllMuscles(): MutableList<MuscleJoint>{
        val cursor = db.rawQuery(DBQueries.DBOperations.getAllMuscles(), null)
        val muscles = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map { MuscleJoint.withCursor(it, isMuscle = true) }
            .toMutableList()
        cursor.close()
        return muscles
    }

    /**
     * Method used to remove a muscle from the database. Will not remove a muscle if the muscle is used to define an exercise (ie defines the primary_mover or is a secondary_mover)
     * @param muscle muscle data class containing the id and name of the muscle to be removed
     * @return if the muscle defines an exercise 2, if the deletion is handled with no error 1, if the deletion has an error 0
     */
    fun removeMuscle(muscle: MuscleJoint): Int{
        val cursor = db.rawQuery(DBQueries.DBOperations.getMuscleUsage(muscle.id), null)
        val result =  if (cursor.moveToFirst())
            2
        else {
            if (deleteMuscle(muscle)) 1 else 0
        }
        cursor.close()
        return result
    }

    /**
     * Method to check if a new or updated muscle is in conflict with an existing muscle
     * @param muscle MuscleJoint with the new or updated muscle information
     * @return true if there is a conflict, false if no conflict found
     */
    fun checkMuscleConflict(muscle: MuscleJoint): Boolean {
        val cursor = db.rawQuery(DBQueries.DBOperations.getMuscleConflict(muscle.name, muscle.id), null)
        val result = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun insertMuscle(muscle: MuscleJoint): Boolean = trySQLCommand(DBQueries.DBOperations.insertMuscle(muscle.name))
    fun updateMuscle(muscle: MuscleJoint): Boolean = trySQLCommand(DBQueries.DBOperations.updateMuscle(muscle.name, muscle.id))
    private fun deleteMuscle(muscle: MuscleJoint): Boolean = trySQLCommand(DBQueries.DBOperations.deleteMuscle(muscle.id))

    /**
     * Method to get a Client object given a client id by searching for that client's information. If a client is not found with the given id, a blank client is returned
     * @param id id of the client to be found
     * @return Client object for the given client id
     */
    fun getClient(id: Int): Client {
        val cursor = db.rawQuery(DBQueries.DBOperations.getClient(id), null)
        val client: Client = if (cursor.moveToFirst())//if a record is found, return a populated Client object
            Client.withCursor(cursor)
        else//if no client found with given id, return an empty object
            Client.empty
        cursor.close()
        return client
    }

    /**
     * Method to get all clients currently in the Clients Table
     * @return MutableList of Client objects for all defined clients
     */
    fun getAllClients(): MutableList<Client> {
        val cursor = db.rawQuery(DBQueries.DBOperations.getAllClients(), null)
        val clients = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map { Client.withCursor(it) }
            .toMutableList()
        cursor.close()
        return clients
    }

    private fun addClientBank(clientID: Int): Boolean = trySQLCommand(DBQueries.DBOperations.addClientBank(clientID))
    fun decClientBank(clientID: Int): Boolean = trySQLCommand(DBQueries.DBOperations.decClientBank(clientID))

    /**
     * Method to get the client type. 1 = weekly constant schedule, 2 = weekly variable session, 3 = monthly variable schedule
     * @param clientID client id value. See Clients Table
     * @return Int value representing the schedule type that a given client follows
     */
    fun getClientType(clientID: Int): ScheduleType {
        val cursor = db.rawQuery(DBQueries.DBOperations.getClientType(clientID), null)
        val result: ScheduleType = if (cursor.moveToFirst())
            //if record found with given client id, return corresponding ScheduleType
            Client.getScheduleType(cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SCHEDULE_TYPE)))
        else
            //if no record found, return BLANK or -1
            ScheduleType.BLANK
        cursor.close()
        return result
    }

    /**
     * Method to check if a client conflicts with the given client list. Only looks at clients with constant schedules (ie session_type = 1)
     * @param client Client object created from data collected in the AddEditClientActivity
     * @return empty String if no conflicts found or client does not have a WEEKLY_CONSTANT schedule
     * non-empty String with conflict client names if any conflicts are found
     */
    fun checkClientConflict(client: Client): String {
        if (client.schedule.scheduleType != ScheduleType.WEEKLY_CONSTANT) return ""
        val conflicts = StringBuilder()
        val sessionDays = client.schedule.sessionDays
        val days = StaticFunctions.NumToDay.slice(1 until 8).filterIndexed { index, _ ->
            sessionDays.any { data -> data.day == index }
        }
        val cursor = db.rawQuery(DBQueries.DBOperations.getClientConflict(client.schedule.getCheckClientConflictDays(), client.id), null)
        val posConflicts = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map {
                Pair (
                    it.getString(1),
                    days.map { s ->
                        val strDay = s.toLowerCase(Locale.ROOT)
                        val time = cursor.getInt(cursor.getColumnIndex(strDay))
                        val duration = cursor.getInt(cursor.getColumnIndex("${strDay}_duration"))
                        time until (time+duration)
                    }
                )
            }
        posConflicts.forEach { p ->
            p.second.forEachIndexed { i, range ->
                if (StaticFunctions.compareTimeRanges(range, sessionDays[i].range) && !conflicts.contains(p.first)){
                    conflicts.append("${p.first},")
                    return@forEachIndexed
                }
            }
        }
        cursor.close()
        //if conflicts are found return the conflicts string omitting the trailing ',' character
        return if (conflicts.isNotEmpty()) conflicts.substring(0 until conflicts.lastIndex)
        //if no conflicts are found return the empty string to signify no conflicts
        else return ""
    }


    fun insertClient(client: Client): Boolean = trySQLCommand(client.getInsertCommand())
    fun updateClient(client: Client): Boolean = trySQLCommand(client.getUpdateCommand())
    fun deleteClient(client: Client): Boolean = trySQLCommand(client.getDeleteCommand())

    /**
     * Method to get an Exercises object given and exercise id
     * @param id id value of an exercise. See Exercises Table for more information
     * @return Exercise object for the given id value
     */
    fun getExercise(id: Int): Exercise {
        //gets base Exercise class information and fills in the ExerciseType using a join and Case clause to get the appropriate data (ie if strength, muscle_name and if mobility or stability, joint_name)
        val cursor = db.rawQuery(DBQueries.DBOperations.getExercise(id), null)
        val exercise = if (cursor.moveToFirst())
            Exercise.withCursor(cursor)
        else
            Exercise.empty
        cursor.close()
        return exercise
    }

    /**
     * Checked
     * Method to get all the exercises with in the Exercises Table
     * @return MutableList of Exercise objects representing the entire Exercise library
     */
    fun getAllExercises(): List<Exercise> {
        //gets base Exercise class information and fills in the ExerciseType using a join and Case clause to get the appropriate data (ie if strength, muscle_name and if mobility or stability, joint_name)
        val cursor = db.rawQuery(DBQueries.DBOperations.getAllExercises(), null)
        val exercises = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map {Exercise.withCursor(it) }
            .toList()
        cursor.close()
        return exercises
    }

    /**
     * Checked
     * Method to check if a current exercise name is already in the Exercises Table
     * @param exercise Exercise object collected from the user in AddEditExerciseActivity
     * @return true if a conflict is found
     */
    fun checkExerciseConflict(exercise: Exercise): Boolean {
        val cursor = db.rawQuery(DBQueries.DBOperations.getExerciseConflict(exercise.name,exercise.id), null)
        val result = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun updateExercise(exercise: Exercise): Boolean = trySQLCommand(exercise.getUpdateCommand())
    fun insertExercise(exercise: Exercise): Boolean = trySQLCommand(exercise.getInsertCommand())
    private fun deleteExercise(exercise: Exercise): Boolean = trySQLCommand(exercise.getDeleteCommand())

    /**
     * Checked does not remove exercise from sessions. Needs to be added back later when more changes made
     * Method used to remove an exercise from the database. Searches through sessions logged to remove references to the exercise. Places a note in the session to "preserve" the session
     * @param exercise Exercise object used to search through session log and remove exercise from database
     * @return if the removal was successful, true. if an error occurred, false
     */
    fun removeExercise(exercise: Exercise): Boolean {
        //gets base Session class information, fills in the client_name with a join and finds only sessions containing the given exercise.id
        val cursor = db.rawQuery(DBQueries.DBOperations.getExerciseUsage(exercise.id), null)
        val result = if (cursor.moveToFirst()) false else deleteExercise(exercise)
        cursor.close()
        return result
    }

    /**
     * Checked
     * Method to get a Session object given a session id.
     * If the session can not be found with the id, a blank session is generated with the given client id and
     *      daytime value (dayTime format "yyyy-MM-dd HH:mm" 24hour)
     * Used when passing to the SessionActivity
     * @param clientID id value of the session holder
     * @param dayTime day and time in a parsable string
     * @return Session object corresponding to the given values
     */
    fun getSession(clientID: Int, dayTime: String,sessionID: Int): Session {
        //gets base Session class information, fills in the client_name with a join and finds only sessions for a given client and dayTime
        val cursor = db.rawQuery(DBQueries.DBOperations.getSession(sessionID), null)
        val session: Session = if (cursor.moveToFirst()){//if a record is found, populate the Session object
            val session = Session.withCursor(cursor)
            val cursor1 = db.rawQuery(DBQueries.DBOperations.getSessionExercises(session.sessionID), null)
            generateSequence { if (cursor1.moveToNext()) cursor1 else null }
                .forEach {
                    session.addExercise(
                        ExerciseSession.withCursor(it)
                    )
                }
            cursor1.close()
            session
        } else{//if a logged session is not found, create a blank session then fill in the duration using the logic below
            val client = getClient(clientID)
            val session = Session.empty(clientID, client.name, dayTime)
            if (checkChange(session)){//if a change for the given client and dayTime then use the corresponding duration
                val cursor1 = db.rawQuery(DBQueries.DBOperations.getSessionSessionChanges(clientID, dayTime), null)
                cursor1.moveToFirst()
                session.duration = cursor1.getInt(0)
                cursor1.close()
            } else//if no change is found, use the duration found from the client with the given dayTime. If the client is not Weekly_Constant, 0 will be returned
                session.duration = client.getDuration(dayTime)
            session
        }
        cursor.close()
        return session
    }

    /**
     * checked
     * Method used to check if a session conflicts with another session on a given day. Will overlook a single client with multiple session on a day if isSameDate = true (use isSameDate when changing the time)
     * @param session Session object to check for conflicts with itself
     * @param isSameDate true if changing the time, false in all other occasions
     * @return true if conflict found. false if no conflict found
     */
    fun checkSessionConflict(session: Session, isSameDate: Boolean): Boolean {
        val day = getScheduleByDay(session.date)
        return day.checkConflict(session, isSameDate)
    }

    /**
     * checked
     * Method to check if a session exists in the Session_log
     * @param session Session object to be checked
     * @return true if session found in log, false if not found
     */
    fun checkSessionLog(session: Session): Boolean {
        val cursor = db.rawQuery(DBQueries.DBOperations.checkSessionLog(session.sessionID), null)
        val result = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun insertSession(session: Session): Boolean = trySQLCommands(session.getSQLCommands(Session.INSERT_COMMAND))
    fun updateSession(session: Session): Boolean = trySQLCommands(session.getSQLCommands(Session.UPDATE_COMMAND))
    private fun deleteSession(session: Session): Boolean = trySQLCommands(session.getSQLCommands(Session.DELETE_COMMAND))

    /**
     * checked
     * Method to cancel a given session. Course of action is dependent on the client's session_type. See Client Table for more information on session_type
     * @param session Session object to be canceled
     * @return true if cancellation was successful
     */
    fun cancelSession(session: Session): Boolean {
        return when(getClientType(session.clientID)){
            ScheduleType.WEEKLY_CONSTANT ->  {
                //if any of the transactions fail, return false. if all transactions pass, return true
                //if change record exists, remove change from session changes
                if (checkChange(session)) if (!deleteChange(session)) return false
                //if session found in log, remove session from log
                if (checkSessionLog(session)) if (!deleteSession(session)) return false
                //increment banked_sessions field
                addClientBank(session.clientID)
            }
            ScheduleType.WEEKLY_VARIABLE -> {
                deleteSession(session)

            }
            ScheduleType.MONTHLY_VARIABLE -> {
                deleteSession(session)

            }
            ScheduleType.NO_SCHEDULE -> deleteSession(session)//delete session if not Weekly_Constant
            ScheduleType.BLANK -> false //return false, error occurred. No ScheduleType Set
        }
    }

    /**
     * checked
     * Method to get all logged sessions belonging to a client
     * @param client Client object representing the client who to be searched for logged sessions
     * @return MutableList of Session objects representing all the sessions logged for a client
     */
    fun getClientSessions(client: Client): List<Session> {
        //gets base Session class information, fills in the client_name with a join and finds only sessions for a given client
        var cursor = db.rawQuery(DBQueries.DBOperations.getClientSessions(client.id), null)
        val sessions = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map { Session.withCursor(it) }
            .toList()
        cursor.close()
        val sessionIDs = sessions.filter { it.sessionID != 0 }.joinToString(",")
        cursor = db.rawQuery(DBQueries.DBOperations.getMultiSessionsExercises(sessionIDs), null)
        generateSequence { if (cursor.moveToNext()) cursor else null }
            .forEach {
                val sessionID = it.getInt(it.getColumnIndex(DBInfo.SessionExercisesTable.SESSION_ID))
                val exerciseSession = ExerciseSession.withCursor(it)
                sessions.find { s -> s.sessionID == sessionID }.let { s -> s?.addExercise(exerciseSession) }
            }
        cursor.close()
        return sessions
    }

    /**
     * checked
     * Method to get all clients that can add a session to a given date
     * @param calendar Calendar object holding a given date to check
     * @return List of clients as Clients objects representing all clients that can add a session to a date
     */
    fun getAddSessionsClientsByDay(calendar: Calendar): List<Client> {
        val day = getScheduleByDay(calendar)
        val cursor = db.rawQuery(DBQueries.DBOperations.getAddSessionClients(day.getStrIDs()), null)
        val clients = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map {Client.withCursor(it) }
            .toList()
        cursor.close()
        return clients
    }

    /**
     * checked
     * Method to get all the sessions corresponding to a given date. Hierarchy of data is Session Log, Normal Schedule from client info (excluding
     * clients who already have sessions logged or if changes have been made to the schedule) then the session changes made (excluding clients who
     * already have sessions logged). If the sessions are not obtained from Session_log, ExerciseSession's List and notes String are left empty
     * @param calendar Calendar object representing a date to find the corresponding sessions
     * @return Day object containing the MutableList of session objects obtained from session queries
     */
    fun getScheduleByDay(calendar: Calendar): Day {
        val sessions = mutableListOf<Session>()
        val calendarNow = Calendar.getInstance()
        val calendarChosen = Calendar.getInstance()
        calendarChosen.time = calendar.time
        val currentDate = (calendarNow[Calendar.YEAR] * 365) + calendarNow[Calendar.DAY_OF_YEAR]
        val chosenDate = (calendarChosen[Calendar.YEAR] * 365) + calendarChosen[Calendar.DAY_OF_YEAR]

        val date = StaticFunctions.getStrDateTime(calendarChosen)
        var cursor = db.rawQuery(DBQueries.DBOperations.getScheduleByDaySessionLog(date), null)
        generateSequence { if (cursor.moveToNext()) cursor else null }
            .forEach {
                sessions.add(
                    Session.withCursor(it)
                )
            }
        cursor.close()
        if (sessions.size > 0) {
            val sessionIDs = sessions.filter { it.sessionID != 0 }.joinToString(",") { it.sessionID.toString()}
            cursor = db.rawQuery(DBQueries.DBOperations.getMultiSessionsExercises(sessionIDs), null)
            generateSequence { if (cursor.moveToNext()) cursor else null }
                .forEach {
                    val sessionID = it.getInt(it.getColumnIndex(DBInfo.SessionExercisesTable.SESSION_ID))
                    val exerciseSession = ExerciseSession.withCursor(it)
                    sessions.find { s -> s.sessionID == sessionID }.let { s -> s?.addExercise(exerciseSession) }
                }
        }
        if (chosenDate >= currentDate) {
            val dayOfWeek = StaticFunctions.NumToDay[calendarChosen[Calendar.DAY_OF_WEEK]]
            cursor = db.rawQuery(DBQueries.DBOperations.getScheduleByDayClients(date, dayOfWeek.toLowerCase(Locale.ROOT)), null)
            generateSequence { if (cursor.moveToNext()) cursor else null }
                .forEach {
                    val time = it.getInt(it.getColumnIndex(dayOfWeek.toLowerCase(Locale.ROOT)))
                    val duration = it.getInt(it.getColumnIndex("${dayOfWeek.toLowerCase(Locale.ROOT)}_duration"))
                    val minutes = time % 60
                    val hour = (time - minutes) / 60
                    calendarChosen[Calendar.HOUR_OF_DAY] = hour
                    calendarChosen[Calendar.MINUTE] = minutes
                    sessions.add(
                        Session.withCursor(
                            it,
                            sessionID = 0,
                            dayTime = StaticFunctions.getStrDateTime(calendarChosen),
                            notes = "",
                            duration = duration
                        )
                    )
                }
            cursor.close()
            cursor = db.rawQuery(DBQueries.DBOperations.getScheduleByDaySessionChanges(date), null)
            generateSequence { if (cursor.moveToNext()) cursor else null }
                .forEach {
                    val dayTime = it.getString(it.getColumnIndex(DBInfo.SessionChangesTable.CHANGE_DAYTIME))
                    val duration = it.getInt(it.getColumnIndex(DBInfo.SessionChangesTable.DURATION))
                    sessions.add(
                        Session.withCursor(
                            it,
                            sessionID = 0,
                            dayTime = dayTime,
                            notes = "",
                            duration = duration
                        )
                    )
                }
            cursor.close()
        }

        return Day(sessions)
    }

    /**
     * checked
     * Method to check if a session exists in the Session_Changes Table
     * @param session Session object containing the information about a session
     * @return true if session exists
     */
    fun checkChange(session: Session): Boolean {
        val cursor = db.rawQuery(DBQueries.DBOperations.checkChange(session.clientID, StaticFunctions.getStrDateTime(session.date)), null)
        val result: Boolean = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun insertChange(oldSession: Session, newSession: Session): Boolean {
        return trySQLCommand(DBQueries.DBOperations.insertChange(oldSession.clientID,
                                                            StaticFunctions.getStrDateTime(oldSession.date),
                                                            StaticFunctions.getStrDateTime(newSession.date),
                                                            newSession.duration))
    }
    fun updateChange(oldSession: Session, newSession: Session): Boolean {
        return trySQLCommand(DBQueries.DBOperations.updateChange(newSession.clientID,
                                                            StaticFunctions.getStrDateTime(newSession.date),
                                                            StaticFunctions.getStrDateTime(oldSession.date),
                                                            newSession.duration))
    }
    private fun deleteChange(session: Session): Boolean {
        return trySQLCommand(DBQueries.DBOperations.deleteChange(session.clientID,
                                                            StaticFunctions.getStrDateTime(session.date)))
    }

    /**
     * checked
     * Method to get the most recent values of an exercise that was completed by a given client
     * @param exercise Exercise object used to get the exercise id and to populate the ExerciseSession object
     * @param clientID id of the given client used to search Session_log table
     * @return ExerciseSession object containing the information about the last occurrence of the given exercise. All non-Exercise object fields are blank if the client has not already performed the exercise
     */
    fun getLastOccurrence(exercise: Exercise, clientID: Int): ExerciseSession {
        val cursor = db.rawQuery(DBQueries.DBOperations.getLastOccurrence(clientID, exercise.id), null)
        val exerciseSession: ExerciseSession = if (cursor.moveToFirst()){
            ExerciseSession.withCursor(cursor)
        } else
            ExerciseSession.empty(exercise)
        cursor.close()
        return exerciseSession
    }

    /**
     * checked
     * Method to get all the occurrences of a given exercise with a given client. Used for progress tracking
     * @param exercise Exercise object used to get the exercise id and to populate the ExerciseSession object
     * @param clientID id of the given client used to search Session_log table
     * @return ExerciseSession object containing the information about the all the occurrences of the given exercise. Empty MutableList returned if the given exercise has been performed
     */
    fun getAllOccurrences(exercise: Exercise, clientID: Int): MutableList<ExerciseSession> {
        val cursor = db.rawQuery(DBQueries.DBOperations.getAllOccurrences(clientID, exercise.id), null)
        val exerciseSessions = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map { ExerciseSession.withCursor(exercise, it) }
            .toMutableList()
        cursor.close()
        return exerciseSessions
    }

    fun cleanSessionChanges(): Boolean {
        val calendar = Calendar.getInstance()
        val date = StaticFunctions.getStrDate(calendar)
        return trySQLCommand(DBQueries.DBOperations.cleanSessionChanges(date))
    }

    fun getAllPrograms(): List<Program> {
        var cursor = db.rawQuery(DBQueries.DBOperations.getAllPrograms, null)
        val programs = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map { Program.withCursor(it) }
            .toMutableList()
        cursor.close()
        if (programs.size > 0 ) {
            val programIDS = programs.filter { it.id != 0 }.joinToString(",") { it.id.toString()}
            cursor = db.rawQuery(DBQueries.DBOperations.getMultiProgramsExercises(programIDS), null)
            generateSequence { if (cursor.moveToNext()) cursor else null }
                .forEach {
                    val programID = it.getInt(it.getColumnIndex(DBInfo.ProgramExercisesTable.PROGRAM_ID))
                    val exerciseProgram = ExerciseProgram.withCursor(it)
                    programs.find { p -> p.id == programID }.let { p -> p?.addExercise(exerciseProgram) }
                }
            cursor.close()
        }
        return programs
    }

    fun getProgram(id: Int): Program {
        var cursor = db.rawQuery(DBQueries.DBOperations.getProgram(id), null)
        val program: Program = if (cursor.moveToFirst()){
            Program(
                cursor.getInt(cursor.getColumnIndex(DBInfo.ProgramsTable.ID)),
                cursor.getString(cursor.getColumnIndex(DBInfo.ProgramsTable.NAME)),
                cursor.getInt(cursor.getColumnIndex(DBInfo.ProgramsTable.DAYS)),
                cursor.getString(cursor.getColumnIndex(DBInfo.ProgramsTable.DESC)),
                mutableListOf()
            )
        } else {
            Program.empty
        }
        cursor.close()
        if (program.id > 0) {
            cursor = db.rawQuery(DBQueries.DBOperations.getMultiProgramsExercises(program.id.toString()), null)
            generateSequence { if (cursor.moveToNext()) cursor else null }
                .forEach { program.addExercise(ExerciseProgram.withCursor(it)) }
            cursor.close()
        }
        return program
    }

    fun removeProgram(program: Program) = trySQLCommands(program.getDeleteProgramCommands())
}