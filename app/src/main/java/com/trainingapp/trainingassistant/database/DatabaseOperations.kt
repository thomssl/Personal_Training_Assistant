package com.trainingapp.trainingassistant.database

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.StaticFunctions
import com.trainingapp.trainingassistant.enumerators.ExerciseType
import com.trainingapp.trainingassistant.enumerators.ScheduleType
import com.trainingapp.trainingassistant.objects.*
import java.util.*
import kotlin.collections.ArrayList

class DatabaseOperations(val context: Context) {

    private var databaseHelper = DatabaseHelper(context)
    private var db: SQLiteDatabase

    init {
        db = databaseHelper.writableDatabase
    }

    /**
     * Checked
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
     * Checked
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

    /**
     * Checked
     * Private method to get the ScheduleType enum from an int obtained from the database
     * @param type Int representation of the ScheduleType
     * @return corresponding ScheduleType of the Int parameter
     */
    private fun getScheduleType(type: Int): ScheduleType{
        return when (type){
            0 -> ScheduleType.NO_SCHEDULE
            1 -> ScheduleType.WEEKLY_CONSTANT
            2 -> ScheduleType.WEEKLY_VARIABLE
            3 -> ScheduleType.MONTHLY_VARIABLE
            else -> ScheduleType.NO_SCHEDULE
        }
    }

    private fun getSecondaryMoversFromCSV(id: Int, csvSecondaryMoversIDs: String, csvSecondaryMoversNames: String): ArrayList<MuscleJoint>{
        val secondaryMovers = ArrayList<MuscleJoint>()
        if (id == 0 || csvSecondaryMoversIDs == "0")//if the id = 0 that means no exercise was found. if strSecondaryMovers is empty than no secondary movers are present. Either way, return a blank ArrayList is returned
            return secondaryMovers
        val lstSecondaryMoversIDs = StaticFunctions.toArrayListInt(csvSecondaryMoversIDs)
        val lstSecondaryMoversNames = csvSecondaryMoversNames.split(",")
        lstSecondaryMoversIDs.forEachIndexed { index, s -> secondaryMovers.add(MuscleJoint(s, lstSecondaryMoversNames[index])) }
        return secondaryMovers
    }

    /**
     * Needs to be updated for multiple users?
     * Method to get the user settings from the database
     * @return ArrayList of the user settings as Ints
     */
    fun getUserSettings(): ArrayList<Int>{
        val cursor = db.rawQuery(DBQueries.DBOperations.getUserSettings(), null)
        val result = if (cursor.moveToFirst()) ArrayList<Int>(listOf(
            cursor.getInt(cursor.getColumnIndex(DBInfo.UserSettingsTable.DEFAULT_DURATION)),
            cursor.getInt(cursor.getColumnIndex(DBInfo.UserSettingsTable.CLOCK_24)))
        )
        else
            ArrayList()
        cursor.close()
        return result
    }

    fun setUserSettings(settings: ArrayList<Int>): Boolean = trySQLCommand(DBQueries.DBOperations.setUserSettings(settings[0],settings[1]))

    /**
     * Checked
     * Method to get the name of joint given the joint id. See Joints Table for more information
     * @param id id of the joint as found in the Joints Table
     * @return joint name corresponding with the given id
     */
    fun getJoint(id: Int): MuscleJoint {
        val cursor = db.rawQuery(DBQueries.DBOperations.getJoint(id), null)
        val result = if (cursor.moveToFirst()) MuscleJoint(
            cursor.getInt(cursor.getColumnIndex(DBInfo.JointsTable.ID)),
            cursor.getString(cursor.getColumnIndex(DBInfo.JointsTable.NAME))
        ) else MuscleJoint(
            0,
            ""
        )
        cursor.close()
        return result
    }

    /**
     * Checked
     * Method to get all the joints found in the database
     * @return ArrayList of MuscleJoint data objects for all the joints defined
     */
    fun getAllJoints(): ArrayList<MuscleJoint>{
        val joints = ArrayList<MuscleJoint>()
        val cursor = db.rawQuery(DBQueries.DBOperations.getAllJoints(), null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                joints.add(
                    MuscleJoint(
                        cursor.getInt(cursor.getColumnIndex(DBInfo.JointsTable.ID)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.JointsTable.NAME))
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        return joints
    }

    /**
     * Checked
     * Method to get a single muscle from the Muscles Table based upon a given id
     * @param id id of the muscle to be found
     * @return MuscleJoint data object representing the desired muscle
     */
    fun getMuscle(id: Int): MuscleJoint {
        val cursor = db.rawQuery(DBQueries.DBOperations.getMuscle(id), null)
        val muscle = if (cursor.moveToFirst()) MuscleJoint(//if muscle found return populated MuscleJoint object
            cursor.getInt(cursor.getColumnIndex(DBInfo.MusclesTable.ID)),
            cursor.getString(cursor.getColumnIndex(DBInfo.MusclesTable.NAME))
        ) else MuscleJoint(//if no muscle found with given id, return an empty object
            0,
            ""
        )
        cursor.close()
        return muscle
    }

    /**
     * Checked
     * Method to get all the muscles from the Muscles Table
     * @return ArrayList of MuscleJoint data objects representing all the defined muscles
     */
    fun getAllMuscles(): ArrayList<MuscleJoint>{
        val muscles = ArrayList<MuscleJoint>()
        val cursor = db.rawQuery(DBQueries.DBOperations.getAllMuscles(), null)
        if (cursor.moveToFirst()) {
            while(!cursor.isAfterLast) {
                muscles.add(
                    MuscleJoint(
                        cursor.getInt(cursor.getColumnIndex(DBInfo.MusclesTable.ID)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.MusclesTable.NAME))
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        return muscles
    }

    /**
     * Checked
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
     * Checked
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
     * Checked
     * Method to get a Client object given a client id by searching for that client's information. If a client is not found with the given id, a blank client is returned
     * @param id id of the client to be found
     * @return Client object for the given client id
     */
    fun getClient(id: Int): Client {
        val cursor = db.rawQuery(DBQueries.DBOperations.getClient(id), null)
        val client: Client = if (cursor.moveToFirst())//if a record is found, return a populated Client object
            Client(
                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.ID)),
                cursor.getString(cursor.getColumnIndex(DBInfo.ClientsTable.NAME)),
                Schedule(
                    getScheduleType(cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SCHEDULE_TYPE))),
                    cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.DAYS)),
                    cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.DURATION)),
                    ArrayList(listOf(
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SUN)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.MON)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.TUE)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.WED)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.THU)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.FRI)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SAT))
                    )),
                    ArrayList(listOf(
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SUN_DURATION)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.MON_DURATION)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.TUE_DURATION)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.WED_DURATION)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.THU_DURATION)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.FRI_DURATION)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SAT_DURATION))
                    ))
                ),
                cursor.getString(cursor.getColumnIndex(DBInfo.ClientsTable.START_DATE)),
                cursor.getString(cursor.getColumnIndex(DBInfo.ClientsTable.END_DATE))
            )
        else//if no client found with given id, return an empty object
            Client(
                0,
                "",
                Schedule(
                    ScheduleType.NO_SCHEDULE,
                    0,
                    0,
                    ArrayList(),
                    ArrayList()
                ),
                "",
                ""
            )
        cursor.close()
        return client
    }

    /**
     * Checked
     * Method to get all clients currently in the Clients Table
     * @return ArrayList of Client objects for all defined clients
     */
    fun getAllClients(): ArrayList<Client>{
        val cursor = db.rawQuery(DBQueries.DBOperations.getAllClients(), null)
        val clients = ArrayList<Client>()
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                clients.add(
                    Client(
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.ID)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ClientsTable.NAME)),
                        Schedule(
                            getScheduleType(cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SCHEDULE_TYPE))),
                            cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.DAYS)),
                            cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.DURATION)),
                            ArrayList(listOf(
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SUN)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.MON)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.TUE)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.WED)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.THU)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.FRI)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SAT))
                            )),
                            ArrayList(listOf(
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SUN_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.MON_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.TUE_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.WED_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.THU_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.FRI_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SAT_DURATION))
                            ))
                        ),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ClientsTable.START_DATE)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ClientsTable.END_DATE))
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        return clients
    }

    private fun addClientBank(clientID: Int): Boolean = trySQLCommand(DBQueries.DBOperations.addClientBank(clientID))
    fun decClientBank(clientID: Int): Boolean = trySQLCommand(DBQueries.DBOperations.decClientBank(clientID))

    /**
     * Checked
     * Method to get the client type. 1 = weekly constant schedule, 2 = weekly variable session, 3 = monthly variable schedule
     * @param clientID client id value. See Clients Table
     * @return Int value representing the schedule type that a given client follows
     */
    private fun getClientType(clientID: Int): ScheduleType{
        val cursor = db.rawQuery(DBQueries.DBOperations.getClientType(clientID), null)
        val result: ScheduleType = if (cursor.moveToFirst()) getScheduleType(cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SCHEDULE_TYPE)))//if record found with given client id, return corresponding ScheduleType
        else ScheduleType.BLANK//if no record found, return BLANK or -1
        cursor.close()
        return result
    }

    /**
     * Checked
     * Method to check if a client conflicts with the given client list. Only looks at clients with constant schedules (ie session_type = 1)
     * Should only be called if the client's ScheduleType is Weekly_Constant
     * @param client Client object created from data collected in the AddEditClientActivity
     * @return empty string if a conflict is found with the current constant schedule. String with conflict client names if any conflicts are found
     */
    fun checkClientConflict(client: Client): String{
        var conflicts = ""
        val cursor = db.rawQuery(DBQueries.DBOperations.getClientConflict(client.schedule.getCheckClientConflictDays(), client.id), null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                client.schedule.daysList.forEachIndexed { index, i ->
                    if (i > 0){
                        val strDay = StaticFunctions.NumToDay[index+1].toLowerCase(Locale.ROOT)
                        val time = cursor.getInt(cursor.getColumnIndex(strDay))
                        val duration = cursor.getInt(cursor.getColumnIndex("${strDay}_duration"))
                        if (StaticFunctions.compareTimeRanges(time..(time+duration),i until (i + client.schedule.durationsList[index])))//compare the time range of the found client with the new/updated client
                            conflicts += "${cursor.getString(2)},"//if there is overlap, add the existing client's name to the conflicts string
                    }
                }
                cursor.moveToNext()
            }
            cursor.close()
        }
        return if (conflicts.isNotEmpty()) conflicts.substring(0 until conflicts.lastIndex)//if conflicts are found return the conflicts string omitting the trailing ',' character
        else return conflicts//if no conflicts are found return the empty string to signify no conflicts
    }


    fun insertClient(client: Client): Boolean = trySQLCommand(client.getInsertCommand())
    fun updateClient(client: Client): Boolean = trySQLCommand(client.getUpdateCommand())
    fun deleteClient(client: Client): Boolean = trySQLCommand(client.getDeleteCommand())

    /**
     * Checked
     * Private method to get the ExerciseType enum from an int obtained from the database
     * @param type Int representation of the ExerciseType
     * @return corresponding ExerciseType of the Int parameter
     */
    private fun getExerciseType(type: Int): ExerciseType{
        return when(type){
            1 -> ExerciseType.STRENGTH
            2 -> ExerciseType.MOBILITY
            3 -> ExerciseType.STABILITY
            else -> ExerciseType.BLANK
        }
    }

    /**
     * Checked
     * Method to get an Exercises object given and exercise id
     * @param id id value of an exercise. See Exercises Table for more information
     * @return Exercise object for the given id value
     */
    fun getExercise(id: Int): Exercise {
        //gets base Exercise class information and fills in the ExerciseType using a join and Case clause to get the appropriate data (ie if strength, muscle_name and if mobility or stability, joint_name)
        val cursor = db.rawQuery(DBQueries.DBOperations.getExercise(id), null)
        val exercise = if (cursor.moveToFirst()) {
            val exerciseID = cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.ID))
            val primaryMover = MuscleJoint(
                cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.PRIMARY_MOVER)),
                cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.PRIMARY_MOVER_NAME))
            )
            Exercise(
                exerciseID,
                cursor.getString(cursor.getColumnIndex(DBInfo.ExercisesTable.NAME)),
                getExerciseType(cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.TYPE))),
                primaryMover,
                getSecondaryMoversFromCSV(exerciseID, cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_IDS)), cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_NAMES)))
            )
        }
        else Exercise(
            0,
            "",
            ExerciseType.BLANK,
            MuscleJoint(0,""),
            ArrayList()
        )
        cursor.close()
        return exercise
    }

    /*
     * Not needed
     * Private method to get many exercises using a csv String as a list. Used to populate a SessionExercise
     * @param strIDs csv string with the ids of many exercises
     * @return ArrayList of Exercise objects representing the list of exercise ids
     */
    /*private fun getManyExercises(strIDs: String): ArrayList<Exercise>{
        val exercises = ArrayList<Exercise>()
        //gets base Exercise class information and fills in the ExerciseType using a join and Case clause to get the appropriate data (ie if strength, muscle_name and if mobility or stability, joint_name)
        val cursor = db.rawQuery("Select e.exercise_id, e.exercise_name, e.exercise_type, e.primary_mover, e.secondary_movers, Case When e.exercise_type = 1 then m.muscle_name When e.exercise_type = 2 or e.exercise_type = 3 then j.joint_name End From Exercises e inner join Exercise_Types t on e.exercise_type=t.exercise_type_id left join Muscles m on e.primary_mover=m.muscle_id left join Joints j on e.primary_mover=j.joint_id Where e.exercise_id in ($strIDs)", null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                val id = cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesEntry.ID))
                val strSecondaryMovers = cursor.getString(cursor.getColumnIndex(DBInfo.ExercisesEntry.SECONDARY_MOVERS))
                val exerciseType = getExerciseType(cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesEntry.TYPE)))
                val primaryMover = MuscleJoint(cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesEntry.PRIMARY_MOVER)), cursor.getString(5))
                exercises.add(
                    Exercise(
                        id,
                        cursor.getString(cursor.getColumnIndex(DBInfo.ExercisesEntry.NAME)),
                        exerciseType,
                        primaryMover,
                        getSecondaryMovers(id,strSecondaryMovers,exerciseType)
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        return exercises
    }*/

    /**
     * Checked
     * Method to get all the exercises with in the Exercises Table
     * @return ArrayList of Exercise objects representing the entire Exercise library
     */
    fun getAllExercises(): ArrayList<Exercise>{
        //gets base Exercise class information and fills in the ExerciseType using a join and Case clause to get the appropriate data (ie if strength, muscle_name and if mobility or stability, joint_name)
        val cursor = db.rawQuery(DBQueries.DBOperations.getAllExercises(), null)
        val exercises = ArrayList<Exercise>()
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                val exerciseID = cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.ID))
                val primaryMover = MuscleJoint(
                    cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.PRIMARY_MOVER)),
                    cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.PRIMARY_MOVER_NAME))
                )
                exercises.add(
                    Exercise(
                        exerciseID,
                        cursor.getString(cursor.getColumnIndex(DBInfo.ExercisesTable.NAME)),
                        getExerciseType(cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.TYPE))),
                        primaryMover,
                        getSecondaryMoversFromCSV(exerciseID, cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_IDS)), cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_NAMES)))
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        return exercises
    }

    /**
     * Checked
     * Method to check if a current exercise name is already in the Exercises Table
     * @param exercise Exercise object collected from the user in AddEditExerciseActivity
     * @return true if a conflict is found
     */
    fun checkExerciseConflict(exercise: Exercise): Boolean{
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
            val session = Session(
                cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogTable.SESSION_ID)),
                cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogTable.CLIENT_ID)),
                cursor.getString(cursor.getColumnIndex(DBInfo.ClientsTable.NAME)),
                cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogTable.DAYTIME)),
                cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogTable.NOTES)),
                cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogTable.DURATION)),
                ArrayList()
            )
            val cursor1 = db.rawQuery(DBQueries.DBOperations.getSessionExercises(session.sessionID), null)
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast){
                    val id = cursor1.getInt(cursor1.getColumnIndex(DBInfo.ExercisesTable.ID))
                    session.addExercise(
                        ExerciseSession(
                            id,
                            cursor1.getString(cursor1.getColumnIndex(DBInfo.ExercisesTable.NAME)),
                            getExerciseType(cursor1.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.TYPE))),
                            MuscleJoint(
                                cursor1.getInt(cursor1.getColumnIndex(DBInfo.ExercisesTable.PRIMARY_MOVER)),
                                cursor1.getString(cursor1.getColumnIndex(DBInfo.AliasesUsed.PRIMARY_MOVER_NAME))
                            ),
                            getSecondaryMoversFromCSV(
                                id,
                                cursor1.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_IDS)),
                                cursor1.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_NAMES))
                            ),
                            cursor1.getString(cursor1.getColumnIndex(DBInfo.SessionExercisesTable.SETS)),
                            cursor1.getString(cursor1.getColumnIndex(DBInfo.SessionExercisesTable.REPS)),
                            cursor1.getString(cursor1.getColumnIndex(DBInfo.SessionExercisesTable.RESISTANCE)),
                            cursor1.getInt(cursor1.getColumnIndex(DBInfo.SessionExercisesTable.EXERCISE_ORDER))
                        )
                    )
                    cursor1.moveToNext()
                }
            }
            cursor1.close()
            session
        } else{//if a logged session is not found, create a blank session then fill in the duration using the logic below
            val client = getClient(clientID)
            val session =
                Session(
                    0,
                    clientID,
                    client.name,
                    dayTime,
                    "",
                    0,
                    ArrayList()
                )
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

    //checked
    //removes the first available makeup session (represented with a 0 in change_dayTime)
    fun removeCanceledSession(clientID: Int): Boolean = trySQLCommand(context.getString(R.string.removeCanceledSessionCommand, clientID))

    /**
     * checked
     * Method used to check if a session conflicts with another session on a given day. Will overlook a single client with multiple session on a day if isSameDate = true (use isSameDate when changing the time)
     * @param session Session object to check for conflicts with itself
     * @param isSameDate true if changing the time, false in all other occasions
     * @return true if conflict found. false if no conflict found
     */
    fun checkSessionConflict(session: Session, isSameDate: Boolean): Boolean{
        val day = getScheduleByDay(session.date)
        return day.checkConflict(session, isSameDate)
    }

    /**
     * checked
     * Method to check if a session exists in the Session_log
     * @param session Session object to be checked
     * @return true if session found in log, false if not found
     */
    fun checkSessionLog(session: Session): Boolean{
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
    fun cancelSession(session: Session): Boolean{
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
     * @return ArrayList of Session objects representing all the sessions logged for a client
     */
    fun getClientSessions(client: Client): ArrayList<Session>{
        val sessions = ArrayList<Session>()
        //gets base Session class information, fills in the client_name with a join and finds only sessions for a given client
        var cursor = db.rawQuery(DBQueries.DBOperations.getClientSessions(client.id), null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                sessions.add(
                    Session(
                        cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogTable.SESSION_ID)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogTable.CLIENT_ID)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ClientsTable.NAME)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogTable.DAYTIME)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogTable.NOTES)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogTable.DURATION)),
                        ArrayList()
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        val sessionIDs = sessions.filter { it.sessionID != 0 }.joinToString(",")
        cursor = db.rawQuery(DBQueries.DBOperations.getMultiSessionsExercises(sessionIDs), null)
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast){
                val sessionID = cursor.getInt(cursor.getColumnIndex(DBInfo.SessionExercisesTable.SESSION_ID))
                val exerciseID = cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.ID))
                val exerciseSession = ExerciseSession(
                    exerciseID,
                    cursor.getString(cursor.getColumnIndex(DBInfo.ExercisesTable.NAME)),
                    getExerciseType(cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.TYPE))),
                    MuscleJoint(
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.PRIMARY_MOVER)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.PRIMARY_MOVER_NAME))
                    ),
                    getSecondaryMoversFromCSV(
                        exerciseID,
                        cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_IDS)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_NAMES))
                    ),
                    cursor.getString(cursor.getColumnIndex(DBInfo.SessionExercisesTable.SETS)),
                    cursor.getString(cursor.getColumnIndex(DBInfo.SessionExercisesTable.REPS)),
                    cursor.getString(cursor.getColumnIndex(DBInfo.SessionExercisesTable.RESISTANCE)),
                    cursor.getInt(cursor.getColumnIndex(DBInfo.SessionExercisesTable.EXERCISE_ORDER))
                )
                sessions.find { it.sessionID == sessionID }.let { it?.addExercise(exerciseSession) }
                cursor.moveToNext()
            }
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
    fun getAddSessionsClientsByDay(calendar: Calendar): ArrayList<Client>{
        val day = getScheduleByDay(calendar)
        val clients = ArrayList<Client>()
        val cursor = db.rawQuery(DBQueries.DBOperations.getAddSessionClients(day.getStrIDs()), null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                clients.add(Client(
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.ID)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ClientsTable.NAME)),
                        Schedule(
                            getScheduleType(cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SCHEDULE_TYPE))),
                            cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.DAYS)),
                            cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.DURATION)),
                            ArrayList(listOf(
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SUN)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.MON)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.TUE)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.WED)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.THU)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.FRI)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SAT))
                            )),
                            ArrayList(listOf(
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SUN_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.MON_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.TUE_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.WED_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.THU_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.FRI_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.SAT_DURATION))
                            ))
                        ),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ClientsTable.START_DATE)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ClientsTable.END_DATE))
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        return clients
    }

    /**
     * checked
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

        val date = StaticFunctions.getStrDateTime(calendarChosen)
        var cursor = db.rawQuery(DBQueries.DBOperations.getScheduleByDaySessionLog(date), null)
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast){
                sessions.add(
                    Session(
                        cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogTable.SESSION_ID)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogTable.CLIENT_ID)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ClientsTable.NAME)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogTable.DAYTIME)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogTable.NOTES)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogTable.DURATION)),
                        ArrayList()
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        if (sessions.size > 0) {
            val sessionIDs = sessions.filter { it.sessionID != 0 }.joinToString(",") { it.sessionID.toString()}
            cursor = db.rawQuery(DBQueries.DBOperations.getMultiSessionsExercises(sessionIDs), null)
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast) {
                    val sessionID = cursor.getInt(cursor.getColumnIndex(DBInfo.SessionExercisesTable.SESSION_ID))
                    val exerciseID = cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.ID))
                    val exerciseSession = ExerciseSession(
                        exerciseID,
                        cursor.getString(cursor.getColumnIndex(DBInfo.ExercisesTable.NAME)),
                        getExerciseType(cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.TYPE))),
                        MuscleJoint(
                            cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.PRIMARY_MOVER)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.PRIMARY_MOVER_NAME))
                        ),
                        getSecondaryMoversFromCSV(exerciseID, cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_IDS)), cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_NAMES))),
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionExercisesTable.SETS)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionExercisesTable.REPS)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionExercisesTable.RESISTANCE)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.SessionExercisesTable.EXERCISE_ORDER))
                    )
                    sessions.find { it.sessionID == sessionID }.let { it?.addExercise(exerciseSession) }
                    cursor.moveToNext()
                }
            }
        }
        if (chosenDate >= currentDate) {
            val dayOfWeek = StaticFunctions.NumToDay[calendarChosen[Calendar.DAY_OF_WEEK]]
            cursor = db.rawQuery(DBQueries.DBOperations.getScheduleByDayClients(date, dayOfWeek.toLowerCase(Locale.ROOT)), null)
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast){
                    val time = cursor.getInt(cursor.getColumnIndex(dayOfWeek.toLowerCase(Locale.ROOT)))
                    val duration = cursor.getInt(cursor.getColumnIndex("${dayOfWeek.toLowerCase(Locale.ROOT)}_duration"))
                    val minutes = time % 60
                    val hour = (time - minutes) / 60
                    calendarChosen[Calendar.HOUR_OF_DAY] = hour
                    calendarChosen[Calendar.MINUTE] = minutes
                    sessions.add(
                        Session(
                            0,
                            cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsTable.ID)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.ClientsTable.NAME)),
                            StaticFunctions.getStrDateTime(calendarChosen),
                            "",
                            duration,
                            ArrayList()
                        )
                    )
                    cursor.moveToNext()
                }
            }
            cursor.close()
            cursor = db.rawQuery(DBQueries.DBOperations.getScheduleByDaySessionChanges(date), null)
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast){
                    sessions.add(
                        Session(
                            0,
                            cursor.getInt(cursor.getColumnIndex(DBInfo.SessionChangesTable.CLIENT_ID)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.ClientsTable.NAME)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.SessionChangesTable.CHANGE_DAYTIME)),
                            "",
                            cursor.getInt(cursor.getColumnIndex(DBInfo.SessionChangesTable.DURATION)),
                            ArrayList()
                        )
                    )
                    cursor.moveToNext()
                }
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
    fun checkChange(session: Session): Boolean{
        val cursor = db.rawQuery(DBQueries.DBOperations.checkChange(session.clientID, StaticFunctions.getStrDateTime(session.date)), null)
        val result: Boolean = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun insertChange(oldSession: Session, newSession: Session): Boolean{
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
            ExerciseSession(
                exercise,
                cursor.getString(cursor.getColumnIndex(DBInfo.SessionExercisesTable.SETS)),
                cursor.getString(cursor.getColumnIndex(DBInfo.SessionExercisesTable.REPS)),
                cursor.getString(cursor.getColumnIndex(DBInfo.SessionExercisesTable.RESISTANCE)),
                cursor.getInt(cursor.getColumnIndex(DBInfo.SessionExercisesTable.EXERCISE_ORDER))
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
     * checked
     * Method to get all the occurrences of a given exercise with a given client. Used for progress tracking
     * @param exercise Exercise object used to get the exercise id and to populate the ExerciseSession object
     * @param clientID id of the given client used to search Session_log table
     * @return ExerciseSession object containing the information about the all the occurrences of the given exercise. Empty ArrayList returned if the given exercise has been performed
     */
    fun getAllOccurrences(exercise: Exercise, clientID: Int): ArrayList<ExerciseSession>{
        val exerciseSessions = ArrayList<ExerciseSession>()
        val cursor = db.rawQuery(DBQueries.DBOperations.getAllOccurrences(clientID, exercise.id), null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                exerciseSessions.add(
                    ExerciseSession(
                        exercise,
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionExercisesTable.SETS)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionExercisesTable.REPS)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionExercisesTable.RESISTANCE)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.SessionExercisesTable.EXERCISE_ORDER))
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        return exerciseSessions
    }

    fun cleanSessionChanges(): Boolean{
        val calendar = Calendar.getInstance()
        val date = StaticFunctions.getStrDate(calendar)
        return trySQLCommand(DBQueries.DBOperations.cleanSessionChanges(date))
    }

    fun getAllPrograms(): ArrayList<Program>{
        val programs = ArrayList<Program>()
        var cursor = db.rawQuery(DBQueries.DBOperations.getAllPrograms, null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                programs.add(
                    Program(
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ProgramsTable.ID)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ProgramsTable.NAME)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ProgramsTable.DAYS)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ProgramsTable.DESC)),
                        ArrayList()
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        if (programs.size > 0 ) {
            val programIDS = programs.filter { it.id != 0 }.joinToString(",") { it.id.toString()}
            cursor = db.rawQuery(DBQueries.DBOperations.getMultiProgramsExercises(programIDS), null)
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast) {
                    val programID = cursor.getInt(cursor.getColumnIndex(DBInfo.ProgramExercisesTable.PROGRAM_ID))
                    val exerciseID = cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.ID))
                    val exerciseProgram = ExerciseProgram(
                        exerciseID,
                        cursor.getString(cursor.getColumnIndex(DBInfo.ExercisesTable.NAME)),
                        getExerciseType(cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.TYPE))),
                        MuscleJoint(
                            cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.PRIMARY_MOVER)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.PRIMARY_MOVER_NAME))
                        ),
                        getSecondaryMoversFromCSV(
                            exerciseID,
                            cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_IDS)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_NAMES))
                        ),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ProgramExercisesTable.SETS)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ProgramExercisesTable.REPS)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ProgramExercisesTable.DAY)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ProgramExercisesTable.EXERCISE_ORDER))
                    )
                    programs.find { it.id == programID }.let { it?.addExercise(exerciseProgram) }
                    cursor.moveToNext()
                }
            }
            cursor.close()
        }
        return programs
    }

    fun getProgram(id: Int): Program{
        var cursor = db.rawQuery(DBQueries.DBOperations.getProgram(id), null)
        val program: Program = if (cursor.moveToFirst()){
            Program(
                cursor.getInt(cursor.getColumnIndex(DBInfo.ProgramsTable.ID)),
                cursor.getString(cursor.getColumnIndex(DBInfo.ProgramsTable.NAME)),
                cursor.getInt(cursor.getColumnIndex(DBInfo.ProgramsTable.DAYS)),
                cursor.getString(cursor.getColumnIndex(DBInfo.ProgramsTable.DESC)),
                ArrayList()
            )
        } else {
            Program(
                0,
                "",
                0,
                "",
                ArrayList()
            )
        }
        cursor.close()
        if (program.id > 0) {
            cursor = db.rawQuery(DBQueries.DBOperations.getMultiProgramsExercises(program.id.toString()), null)
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast) {
                    val exerciseID = cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.ID))
                    val exerciseProgram = ExerciseProgram(
                        exerciseID,
                        cursor.getString(cursor.getColumnIndex(DBInfo.ExercisesTable.NAME)),
                        getExerciseType(cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.TYPE))),
                        MuscleJoint(
                            cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesTable.PRIMARY_MOVER)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.PRIMARY_MOVER_NAME))
                        ),
                        getSecondaryMoversFromCSV(
                            exerciseID,
                            cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_IDS)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.AliasesUsed.SECONDARY_MOVERS_NAMES))
                        ),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ProgramExercisesTable.SETS)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ProgramExercisesTable.REPS)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ProgramExercisesTable.DAY)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ProgramExercisesTable.EXERCISE_ORDER))
                    )
                    program.addExercise(exerciseProgram)
                    cursor.moveToNext()
                }
            }
            cursor.close()
        }
        return program
    }

    fun removeProgram(program: Program) = trySQLCommands(program.getDeleteProgramCommands())
}