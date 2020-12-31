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
            sql.forEach { if (it.isNotBlank()) db.execSQL(it) }
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
     * Needs to be updated for multiple users?
     * Method to get the user settings from the database
     * @return List of the user settings as Ints
     */
    fun getUserSettings(): List<Int>{
        val cursor = db.rawQuery(DBQueries.getUserSettings(), null)
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

    fun setUserSettings(settings: MutableList<Int>): Boolean = trySQLCommand(DBQueries.setUserSettings(settings[0],settings[1]))

/*
    *
     * Method to get the name of joint given the joint id. See Joints Table for more information
     * @param id id of the joint as found in the Joints Table
     * @return joint name corresponding with the given id
*/
/*    fun getJoint(id: Int): MuscleJoint {
        val cursor = db.rawQuery(DBQueries.getJoint(id), null)
        val result = if (cursor.moveToFirst())
            MuscleJoint.withCursor(cursor, isMuscle = false)
        else
            MuscleJoint.empty
        cursor.close()
        return result
    }*/

    /**
     * Method to get all the joints found in the database
     * @return MutableList of MuscleJoint data objects for all the joints defined
     */
    fun getAllJoints(): MutableList<MuscleJoint> {
        val cursor = db.rawQuery(DBQueries.getAllJoints, null)
        val joints = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map { MuscleJoint.withCursor(it, isMuscle = false) }
            .toMutableList()
        cursor.close()
        return joints
    }

/*
    *
     * Method to get a single muscle from the Muscles Table based upon a given id
     * @param id id of the muscle to be found
     * @return MuscleJoint data object representing the desired muscle
*/
/*    fun getMuscle(id: Int): MuscleJoint {
        val cursor = db.rawQuery(DBQueries.getMuscle(id), null)
        val muscle = if (cursor.moveToFirst())
            MuscleJoint.withCursor(cursor, isMuscle = true)
        else
            MuscleJoint.empty
        cursor.close()
        return muscle
    }*/

    /**
     * Method to get all the muscles from the Muscles Table
     * @return MutableList of MuscleJoint data objects representing all the defined muscles
     */
    fun getAllMuscles(): MutableList<MuscleJoint> {
        val cursor = db.rawQuery(DBQueries.getAllMuscles, null)
        val muscles = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map { MuscleJoint.withCursor(it, isMuscle = true) }
            .toMutableList()
        cursor.close()
        return muscles
    }

    /**
     * Method used to remove a muscle from the database. Will not remove a muscle if the muscle is used to define an exercise
     * ie defines the primary_mover or is a secondary_mover
     * @param muscle muscle data class containing the id and name of the muscle to be removed
     * @return if the muscle defines an exercise 2, if the deletion is handled with no error 1, if the deletion has an error 0
     */
    fun removeMuscle(muscle: MuscleJoint): Int{
        val cursor = db.rawQuery(DBQueries.getMuscleUsage(muscle.id), null)
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
        val cursor = db.rawQuery(DBQueries.getMuscleConflict(muscle.name, muscle.id), null)
        val result = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun insertMuscle(muscle: MuscleJoint): Boolean = trySQLCommand(DBQueries.insertMuscle(muscle.name))
    fun updateMuscle(muscle: MuscleJoint): Boolean = trySQLCommand(DBQueries.updateMuscle(muscle.name, muscle.id))
    private fun deleteMuscle(muscle: MuscleJoint): Boolean = trySQLCommand(DBQueries.deleteMuscle(muscle.id))

    /**
     * Method to get a Client object given a client id by searching for that client's information
     * If a client is not found with the given id, a blank client is returned
     * @param id id of the client to be found
     * @return Client object for the given client id
     */
    fun getClient(id: Int): Client {
        val cursor = db.rawQuery(DBQueries.getClient(id), null)
        // If a record is found, return a populated Client object
        val client: Client = if (cursor.moveToFirst())
            Client.withCursor(cursor)
        // If no client found with given id, return an empty Client
        else
            Client.empty
        cursor.close()
        return client
    }

    /**
     * Method to get all clients currently in the Clients Table
     * @return MutableList of Client objects for all defined clients
     */
    fun getAllClients(): List<Client> {
        val cursor = db.rawQuery(DBQueries.getAllClients, null)
        val clients = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map { Client.withCursor(it) }
            .toList()
        cursor.close()
        return clients
    }

    private fun addClientBank(clientID: Int): Boolean = trySQLCommand(DBQueries.addClientBank(clientID))
    fun decClientBank(clientID: Int): Boolean = trySQLCommand(DBQueries.decClientBank(clientID))

    /**
     * Method to get the client type
     * @param clientID client id value. See Clients Table
     * @return Enum value representing the schedule type that a given client follows
     */
    fun getClientType(clientID: Int): ScheduleType {
        val cursor = db.rawQuery(DBQueries.getClientType(clientID), null)
        // If record found with given client id, return corresponding ScheduleType
        val result: ScheduleType = if (cursor.moveToFirst())
            Client.getScheduleType(cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SCHEDULE_TYPE)))
        // If no record found, return BLANK
        else
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
        // If the client passed does not have a WEEKLY_CONSTANT schedule return no conflict
        if (client.scheduleType != ScheduleType.WEEKLY_CONSTANT) return ""
        // Get the schedule's sessionDays as a list of ClientConflictData (ie a list of days as 0-6 and the IntRange for the session time
        val sessionDays = client.sessionDays
        // Get all the day abbreviations (ie mon, tue, wed, etc) that appear in sessionDays. Used to get the possible conflict data for the cursor
        val days = StaticFunctions.NumToDay.slice(1 until 8).filterIndexed { index, _ ->
            sessionDays.any { data -> data.day == index }
        }
        val cursor = db.rawQuery(DBQueries.getClientConflict(client.conflictDays, client.id), null)
        val posConflicts = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map {
                // Map the cursor data as pairs of name and a list of possible conflict ranges for each new client day
                Pair (
                    it.getString(1),
                    days.map { s ->
                        val strDay = s.toLowerCase(Locale.ROOT)
                        val time = it.getInt(it.getColumnIndex(strDay))
                        val duration = it.getInt(it.getColumnIndex("${strDay}_duration"))
                        // If time is > 0, use time and duration. If not, send a range that will never conflict with the new client
                        if (time > 0) time until (time+duration) else -1 until 0
                    }
                )
            }
        cursor.close()
        // Look through every possible conflict and accumulate the names of other clients that create a scheduling conflict.
        return posConflicts.fold(listOf<String>()) { acc, p ->
            // Look through every range for the current client map every instance with a conflict. Only keep 1 instance of the name due to distinct
            acc + p.second.mapIndexedNotNull { i, range ->
                if (StaticFunctions.compareTimeRanges(range, sessionDays[i].range))
                    p.first
                else
                    null
            }.distinct()
        }.joinToString()
    }


    fun insertClient(client: Client): Boolean = trySQLCommand(client.insertCommand)
    fun updateClient(client: Client): Boolean = trySQLCommand(client.updateCommand)
    fun deleteClient(client: Client): Boolean = trySQLCommand(client.deleteCommand)

    /**
     * Method to get an Exercises object given and exercise id
     * @param id id value of an exercise. See Exercises Table for more information
     * @return Exercise object for the given id value
     */
    fun getExercise(id: Int): Exercise {
        // Gets base Exercise class information and fills in the ExerciseType using a join and Case clause to get the appropriate data
        // ie if strength, muscle_name and if mobility or stability, joint_name
        val cursor = db.rawQuery(DBQueries.getExercise(id), null)
        val exercise = if (cursor.moveToFirst())
            Exercise.withCursor(cursor)
        else
            Exercise.empty
        cursor.close()
        return exercise
    }

    /**
     * Method to get all the exercises with in the Exercises Table
     * @return MutableList of Exercise objects representing the entire Exercise library
     */
    fun getAllExercises(): List<Exercise> {
        // Gets base Exercise class information and fills in the ExerciseType using a join and Case clause to get the appropriate data
        // ie if strength, muscle_name and if mobility or stability, joint_name
        val cursor = db.rawQuery(DBQueries.getAllExercises, null)
        val exercises = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map {Exercise.withCursor(it) }
            .toList()
        cursor.close()
        return exercises
    }

    /**
     * Method to check if a current exercise name is already in the Exercises Table
     * @param exercise Exercise object collected from the user in AddEditExerciseActivity
     * @return true if a conflict is found
     */
    fun checkExerciseConflict(exercise: Exercise): Boolean {
        val cursor = db.rawQuery(DBQueries.getExerciseConflict(exercise.name,exercise.id), null)
        val result = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun updateExercise(exercise: Exercise): Boolean = trySQLCommand(exercise.updateCommand)
    fun insertExercise(exercise: Exercise): Boolean = trySQLCommand(exercise.insertCommand)
    private fun deleteExercise(exercise: Exercise): Boolean = trySQLCommand(exercise.deleteCommand)

    /**
     * Checked does not remove exercise from sessions. Needs to be added back later when more changes made
     * Method used to remove an exercise from the database. Searches through sessions logged to remove references to the exercise
     * Places a note in the session to "preserve" the session
     * @param exercise Exercise object used to search through session log and remove exercise from database
     * @return if the removal was successful, true. if an error occurred, false
     */
    fun removeExercise(exercise: Exercise): Boolean {
        //gets base Session class information, fills in the client_name with a join and finds only sessions containing the given exercise.id
        val cursor = db.rawQuery(DBQueries.getExerciseUsage(exercise.id), null)
        val result = if (cursor.moveToFirst()) false else deleteExercise(exercise)
        cursor.close()
        return result
    }

    /**
     * Method to get a Session object given a session id.
     * If the session can not be found with the id, a blank session is generated with the given client id and daytime value
     * Used when passing to the SessionActivity
     * @param clientID id value of the session holder
     * @param dayTime day and time in a parsable string
     * @return Session object corresponding to the given values
     */
    fun getSession(clientID: Int, dayTime: String,sessionID: Int): Session {
        // Gets base Session class information, fills in the client_name with a join and finds only sessions for a given client and dayTime
        val cursor = db.rawQuery(DBQueries.getSession(sessionID), null)
        // If a record is found, populate the Session object
        val session: Session = if (cursor.moveToFirst()){
            val session = Session.withCursor(cursor)
            val cursor1 = db.rawQuery(DBQueries.getSessionExercises(session.sessionID), null)
            val exercises = generateSequence {
                if (cursor1.moveToNext()) cursor1 else null
            }.map { ExerciseSession.withCursor(it) }
            session.addExercises(exercises)
            cursor1.close()
            session

        } else {
            // If a logged session is not found, create a blank session then fill in the duration using the logic below
            val client = getClient(clientID)
            val session = Session.empty(clientID, client.name, dayTime)
            // If a change for the given client and dayTime then use the corresponding duration
            val duration = if (checkChange(session)){
                val cursor1 = db.rawQuery(DBQueries.getSessionSessionChanges(clientID, dayTime), null)
                cursor1.moveToFirst()
                val temp = cursor1.getInt(0)//session.duration = cursor1.getInt(0)
                cursor1.close()
                temp
            } else
                // If no change is found, use the duration found from the client with the given dayTime
                // If the client is not Weekly_Constant, 0 will be returned
                //session.duration = client.getDuration(dayTime)
                client.getDuration(dayTime)
            session.clone(duration = duration)
        }
        cursor.close()
        return session
    }

    /**
     * Method used to check if a session conflicts with another session on a given day
     * Will overlook a single client with multiple session on a day if isSameDate = true (use isSameDate when changing the time)
     * @param session Session object to check for conflicts with itself
     * @param isSameDate true if changing the time, false in all other occasions
     * @return true if conflict found. false if no conflict found
     */
    fun checkSessionConflict(session: Session, isSameDate: Boolean): Boolean {
        val day = getScheduleByDay(session.time)
        return day.checkConflict(session, isSameDate)
    }

    /**
     * Method to check if a session record exists in the Session_log
     * @param session Session object to be checked
     * @return true if session found in log, false if not found
     */
    fun checkSessionLog(session: Session): Boolean {
        val cursor = db.rawQuery(DBQueries.checkSessionLog(session.sessionID), null)
        val result = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun insertSession(session: Session): Boolean = trySQLCommands(session.insertCommands)
    fun updateSession(session: Session): Boolean = trySQLCommands(session.updateCommands)
    private fun deleteSession(session: Session): Boolean = trySQLCommands(session.deleteCommands)

    /**
     * Method to cancel a given session. Course of action is dependent on the client's session_type
     * See Client Table for more information on session_type
     * @param session Session object to be canceled
     * @return true if cancellation was successful
     */
    fun cancelSession(session: Session): Boolean {
        return when(getClientType(session.clientID)){
            ScheduleType.WEEKLY_CONSTANT ->  {
                // If any of the transactions fail, return false. If all transactions pass, return true
                // If change record exists, remove change from session changes
                if (checkChange(session)) if (!deleteChange(session)) return false
                // If session found in log, remove session from log
                if (checkSessionLog(session)) if (!deleteSession(session)) return false
                // Increment banked_sessions field
                addClientBank(session.clientID)
            }
            ScheduleType.WEEKLY_VARIABLE -> {
                deleteSession(session)

            }
            ScheduleType.MONTHLY_VARIABLE -> {
                deleteSession(session)

            }
            // Delete session if not Weekly_Constant
            ScheduleType.NO_SCHEDULE -> deleteSession(session)
            // Return false, error occurred. No ScheduleType Set
            ScheduleType.BLANK -> false
        }
    }

    /**
     * Method to get all logged sessions belonging to a client
     * @param client Client object representing the client who to be searched for logged sessions
     * @return MutableList of Session objects representing all the sessions logged for a client
     */
    fun getClientSessions(client: Client): List<Session> {
        //gets base Session class information, fills in the client_name with a join and finds only sessions for a given client
        var cursor = db.rawQuery(DBQueries.getClientSessions(client.id), null)
        val sessions = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map { Session.withCursor(it) }
            .toList()
        cursor.close()
        val sessionIDs = sessions.mapNotNull { if (it.sessionID != 0) it.sessionID else null }
        cursor = db.rawQuery(DBQueries.getMultiSessionsExercises(sessionIDs.joinToString(",")), null)
        generateSequence {
            if (cursor.moveToNext()) cursor else null
        }.map {
            val sessionID = it.getInt(it.getColumnIndex(DBInfo.SessionExercisesTable.SESSION_ID))
            val exerciseSession = ExerciseSession.withCursor(it)
            Pair(sessionID, exerciseSession)
        }.groupBy(
            { it.first },
            { it.second }
        ).forEach {
            sessions.find { s -> s.sessionID == it.key }.let { s -> s?.addExercises(it.value.asSequence()) }
        }
        cursor.close()
        return sessions
    }

    /**
     * Method to get all clients that can add a session to a given date
     * @param time Date object holding a given date to check
     * @return List of clients as Clients objects representing all clients that can add a session to a date
     */
    fun getAddSessionsClientsByDay(time: Date): List<Client> {
        val day = getScheduleByDay(time)
        val cursor = db.rawQuery(DBQueries.getAddSessionClients(day.strIDs), null)
        val clients = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map { Client.withCursor(it) }
            .toList()
        cursor.close()
        return clients
    }

    /**
     * Method to get all the sessions corresponding to a given date. Hierarchy of data is Session Log, Normal Schedule from client info (excluding
     * clients who already have sessions logged or if changes have been made to the schedule) then the session changes made (excluding clients who
     * already have sessions logged). If the sessions are not obtained from Session_log, ExerciseSession's List and notes String are left empty
     * @param dateTime Date object representing a date to find the corresponding sessions
     * @return Day object containing the MutableList of session objects obtained from session queries
     */
    fun getScheduleByDay(dateTime: Date): Day {
        val sessions = mutableListOf<Session>()
        val calendarNow = Calendar.getInstance()
        val calendarChosen = Calendar.getInstance()
        calendarChosen.time = dateTime
        val currentDate = (calendarNow[Calendar.YEAR] * 365) + calendarNow[Calendar.DAY_OF_YEAR]
        val chosenDate = (calendarChosen[Calendar.YEAR] * 365) + calendarChosen[Calendar.DAY_OF_YEAR]

        val date = StaticFunctions.getStrDateTime(calendarChosen.time)
        var cursor = db.rawQuery(DBQueries.getScheduleByDaySessionLog(date), null)
        generateSequence { if (cursor.moveToNext()) cursor else null }
            .forEach {
                sessions.add( Session.withCursor(it) )
            }
        cursor.close()
        if (sessions.size > 0) {
            val sessionIDs = sessions.filter { it.sessionID != 0 }.joinToString(",") { it.sessionID.toString()}
            cursor = db.rawQuery(DBQueries.getMultiSessionsExercises(sessionIDs), null)
            generateSequence { if (cursor.moveToNext()) cursor else null }
                .forEach {
                    val sessionID = it.getInt(it.getColumnIndex(DBInfo.SessionExercisesTable.SESSION_ID))
                    val exerciseSession = ExerciseSession.withCursor(it)
                    sessions.find { s -> s.sessionID == sessionID }.let { s -> s?.addExercise(exerciseSession) }
                }
        }
        if (chosenDate >= currentDate) {
            val dayOfWeek = StaticFunctions.NumToDay[calendarChosen[Calendar.DAY_OF_WEEK]].toLowerCase(Locale.ROOT)
            cursor = db.rawQuery(DBQueries.getScheduleByDayClients(date, dayOfWeek), null)
            generateSequence { if (cursor.moveToNext()) cursor else null }
                .forEach {
                    val time = it.getInt(it.getColumnIndex(dayOfWeek))
                    val duration = it.getInt(it.getColumnIndex("${dayOfWeek}_duration"))
                    val minutes = time % 60
                    calendarChosen[Calendar.HOUR_OF_DAY] = (time - minutes) / 60
                    calendarChosen[Calendar.MINUTE] = minutes
                    sessions.add(
                        Session.withCursor(
                            it,
                            sessionID = 0,
                            dayTime = StaticFunctions.getStrDateTime(calendarChosen.time),
                            notes = "",
                            duration = duration
                        )
                    )
                }
            cursor.close()
            cursor = db.rawQuery(DBQueries.getScheduleByDaySessionChanges(date), null)
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
     * Method to check if a session exists in the Session_Changes Table
     * @param session Session object containing the information about a session
     * @return true if session exists
     */
    fun checkChange(session: Session): Boolean {
        val cursor = db.rawQuery(DBQueries.checkChange(session.clientID, session.strDayTime), null)
        val result: Boolean = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun insertChange(oldSession: Session, newSession: Session): Boolean {
        return trySQLCommand(
            DBQueries.insertChange(
                oldSession.clientID,
                oldSession.strDayTime,
                newSession.strDayTime,
                newSession.duration
            )
        )
    }
    fun updateChange(oldSession: Session, newSession: Session): Boolean {
        return trySQLCommand(
            DBQueries.updateChange(
                newSession.clientID,
                newSession.strDayTime,
                oldSession.strDayTime,
                newSession.duration
            )
        )
    }
    private fun deleteChange(session: Session): Boolean {
        return trySQLCommand(
            DBQueries.deleteChange(
                session.clientID,
                session.strDayTime
            )
        )
    }

    /**
     * Method to get the most recent values of an exercise that was completed by a given client
     * @param exercise Exercise object used to get the exercise id and to populate the ExerciseSession object
     * @param clientID id of the given client used to search Session_log table
     * @return ExerciseSession object containing the information about the last occurrence of the given exercise
     * All non-Exercise object fields are blank if the client has not already performed the exercise
     */
    fun getLastOccurrence(exercise: Exercise, clientID: Int): ExerciseSession {
        val cursor = db.rawQuery(DBQueries.getLastOccurrence(clientID, exercise.id), null)
        val exerciseSession: ExerciseSession = if (cursor.moveToFirst()){
            ExerciseSession.withCursor(cursor)
        } else
            ExerciseSession.empty(exercise)
        cursor.close()
        return exerciseSession
    }

    /**
     * Method to get all the occurrences of a given exercise with a given client. Used for progress tracking
     * @param exercise Exercise object used to get the exercise id and to populate the ExerciseSession object
     * @param clientID id of the given client used to search Session_log table
     * @return ExerciseSession object containing the information about the all the occurrences of the given exercise
     * Empty MutableList returned if the given exercise has been performed
     */
    fun getAllOccurrences(exercise: Exercise, clientID: Int): MutableList<ExerciseSession> {
        val cursor = db.rawQuery(DBQueries.getAllOccurrences(clientID, exercise.id), null)
        val exerciseSessions = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map { ExerciseSession.withCursor(exercise, it) }
            .toMutableList()
        cursor.close()
        return exerciseSessions
    }

    fun cleanSessionChanges(): Boolean {
        val date = StaticFunctions.getStrDate(Calendar.getInstance().time)
        return trySQLCommand(DBQueries.cleanSessionChanges(date))
    }

    fun getAllPrograms(): List<Program> {
        var cursor = db.rawQuery(DBQueries.getAllPrograms, null)
        val programs = generateSequence { if (cursor.moveToNext()) cursor else null }
            .map { Program.withCursor(it) }
            .toMutableList()
        cursor.close()
        if (programs.size > 0 ) {
            val programIDS = programs.filter { it.id != 0 }.joinToString(",") { it.id.toString()}
            cursor = db.rawQuery(DBQueries.getMultiProgramsExercises(programIDS), null)
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
        var cursor = db.rawQuery(DBQueries.getProgram(id), null)
        val program: Program = if (cursor.moveToFirst()){
            Program.withCursor(cursor)
        } else {
            Program.empty
        }
        cursor.close()
        if (program.id > 0) {
            cursor = db.rawQuery(DBQueries.getMultiProgramsExercises(program.id.toString()), null)
            generateSequence { if (cursor.moveToNext()) cursor else null }
                .forEach { program.addExercise(ExerciseProgram.withCursor(it)) }
            cursor.close()
        }
        return program
    }

    fun removeProgram(program: Program) = trySQLCommands(program.deleteProgramCommands)
}