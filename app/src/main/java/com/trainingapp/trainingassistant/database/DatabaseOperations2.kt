package com.trainingapp.trainingassistant.database

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.StaticFunctions
import com.trainingapp.trainingassistant.enumerators.ExerciseType
import com.trainingapp.trainingassistant.enumerators.ScheduleType
import com.trainingapp.trainingassistant.objects.*
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class DatabaseOperations2(val context: Context) {

    private var databaseHelper = DatabaseHelper2(context)
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
        return try {
            db.execSQL(sql)
            true
        } catch (e:SQLException){
            e.printStackTrace()
            false
        }
    }

    /**
     * Not needed
     * Method to convert csv values for exercise_ids, sets, reps, resistances and exercise_orders into an ArrayList of ExerciseSession Objects. Used to fill a Session object
     * @param ids exercise_ids as csv
     * @param sets sets as csv
     * @param reps reps as csv
     * @param resistances resistances as csv
     * @return ArrayList of ExerciseSession objects populated with the given values
     */
    private fun getListExerciseSessions(ids: String, sets: String, reps: String, resistances: String, orders: String): ArrayList<ExerciseSession>{
        val exercises = ArrayList<ExerciseSession>()
        if (ids.isEmpty()) //if ids is empty the Session does not contain any exercises. Return a blank ArrayList
            return exercises
        val lstExercises = getManyExercises(ids)//gets all the exercises in one query
        val lstSets = sets.split(",")
        val lstReps = reps.split(",")
        val lstResistances = resistances.split(",")
        val lstOrders = StaticFunctions.toArrayListInt(orders)

        for(i in lstExercises.indices)//populate blank ArrayList with ExerciseSession objects created using the processed parameters
            exercises.add(
                ExerciseSession(
                    lstExercises[i],
                    lstSets[i],
                    lstReps[i],
                    lstResistances[i],
                    lstOrders[i]
                )
            )
        return exercises
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

    /**
     * Not Needed
     * Private method to get the list of secondary movers as an ArrayList of MuscleJoint data objects
     * @param id id of the exercise obtained from the database query
     * @param strSecondaryMovers csv String file extracted from the database query
     * @param exerciseType ExerciseType enum value associated with the exercise found from the database query
     * @return ArrayList of MuscleJoint data objects corresponding to the movers csv sent as a parameter
     */
    private fun getSecondaryMovers(id: Int, strSecondaryMovers: String, exerciseType: ExerciseType): ArrayList<MuscleJoint>{
        val secondaryMovers = ArrayList<MuscleJoint>()
        if (id == 0 || strSecondaryMovers == "0"){//if the id = 0 that means no exercise was found. if strSecondaryMovers is empty than no secondary movers are present. Either way, return a blank ArrayList is returned
            return secondaryMovers
        }
        val tableName = if (exerciseType == ExerciseType.STRENGTH) "Muscles" else "Joints"//set table name based upon ExerciseType of query result
        val prefix = if (exerciseType == ExerciseType.STRENGTH) "muscle_" else "joint_" //set field name prefix based upon ExerciseType of query result
        val cursor = db.rawQuery("Select ${prefix}id, ${prefix}name From $tableName Where ${prefix}id in (${strSecondaryMovers})", null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                secondaryMovers.add(MuscleJoint(
                    if (exerciseType == ExerciseType.STRENGTH) cursor.getInt(cursor.getColumnIndex(DBInfo.MusclesEntry.ID)) else cursor.getInt(cursor.getColumnIndex(DBInfo.JointsEntry.ID)),//use appropriate field name base upon ExerciseType of the query result
                    if (exerciseType == ExerciseType.STRENGTH) cursor.getString(cursor.getColumnIndex(DBInfo.MusclesEntry.NAME)) else cursor.getString(cursor.getColumnIndex(DBInfo.JointsEntry.NAME))//use appropriate field name base upon ExerciseType of the query result
                ))
                cursor.moveToNext()
            }
        }
        cursor.close()
        return secondaryMovers
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
        val cursor = db.rawQuery(context.getString(R.string.getUserSettingsQuery), null)
        val result = if (cursor.moveToFirst()) ArrayList<Int>(arrayOf(cursor.getInt(cursor.getColumnIndex(DBInfo2.UserSettingsTable.DEFAULT_DURATION)), cursor.getInt(cursor.getColumnIndex(DBInfo2.UserSettingsTable.CLOCK_24))).asList())
        else
            ArrayList()
        cursor.close()
        return result
    }

    fun setUserSettings(settings: ArrayList<Int>): Boolean = trySQLCommand(context.getString(R.string.setUserSettingsCommand,settings[0],settings[1]))

    /**
     * Checked
     * Method to get the name of joint given the joint id. See Joints Table for more information
     * @param id id of the joint as found in the Joints Table
     * @return joint name corresponding with the given id
     */
    fun getJoint(id: Int): MuscleJoint {
        val cursor = db.rawQuery(context.getString(R.string.getJointQuery, id), null)
        val result = if (cursor.moveToFirst()) MuscleJoint(
            cursor.getInt(cursor.getColumnIndex(DBInfo.JointsEntry.ID)),
            cursor.getString(cursor.getColumnIndex(DBInfo.JointsEntry.NAME))
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
        val cursor = db.rawQuery(context.getString(R.string.getAllJointsQuery), null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                joints.add(
                    MuscleJoint(
                        cursor.getInt(cursor.getColumnIndex(DBInfo.JointsEntry.ID)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.JointsEntry.NAME))
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
        val cursor = db.rawQuery(context.getString(R.string.getMuscleQuery, id), null)
        val muscle = if (cursor.moveToFirst()) MuscleJoint(//if muscle found return populated MuscleJoint object
            cursor.getInt(cursor.getColumnIndex(DBInfo.MusclesEntry.ID)),
            cursor.getString(cursor.getColumnIndex(DBInfo.MusclesEntry.NAME))
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
        val cursor = db.rawQuery(context.getString(R.string.getAllMusclesQuery), null)
        if (cursor.moveToFirst()) {
            while(!cursor.isAfterLast) {
                muscles.add(
                    MuscleJoint(
                        cursor.getInt(cursor.getColumnIndex(DBInfo.MusclesEntry.ID)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.MusclesEntry.NAME))
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
        val cursor = db.rawQuery(context.getString(R.string.removeMuscleQuery, muscle.id), null)
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
        val cursor = db.rawQuery(context.getString(R.string.checkMuscleConflictQuery, muscle.name, muscle.id), null)
        val result = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun insertMuscle(muscle: MuscleJoint): Boolean = trySQLCommand(context.getString(R.string.insertMuscleCommand, muscle.name))
    fun updateMuscle(muscle: MuscleJoint): Boolean = trySQLCommand(context.getString(R.string.updateMuscleCommand, muscle.name, muscle.id))
    private fun deleteMuscle(muscle: MuscleJoint): Boolean = trySQLCommand(context.getString(R.string.deleteMuscleCommand, muscle.id))

    /**
     * Checked
     * Method to get a Client object given a client id by searching for that client's information. If a client is not found with the given id, a blank client is returned
     * @param id id of the client to be found
     * @return Client object for the given client id
     */
    fun getClient(id: Int): Client2 {
        val cursor = db.rawQuery(context.getString(R.string.getClientQuery, id), null)
        val client: Client2 = if (cursor.moveToFirst())//if a record is found, return a populated Client object
            Client2(
                cursor.getInt(cursor.getColumnIndex(DBInfo2.ClientsTable.ID)),
                cursor.getString(cursor.getColumnIndex(DBInfo2.ClientsTable.NAME)),
                Schedule(
                    getScheduleType(cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.SCHEDULE_TYPE))),
                    cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.DAYS)),
                    cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.DURATION)),
                    ArrayList(listOf(
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.SUN)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.MON)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.TUE)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.WED)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.THU)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.FRI)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.SAT))
                    )),
                    ArrayList(listOf(
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.SUN_DURATION)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.MON_DURATION)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.TUE_DURATION)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.WED_DURATION)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.THU_DURATION)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.FRI_DURATION)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.SAT_DURATION))
                    ))
                ),
                cursor.getString(cursor.getColumnIndex(DBInfo2.ClientsTable.START_DATE)),
                cursor.getString(cursor.getColumnIndex(DBInfo2.ClientsTable.END_DATE))
            )
        else//if no client found with given id, return an empty object
            Client2(
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
    fun getAllClients(): ArrayList<Client2>{
        val cursor = db.rawQuery(context.getString(R.string.getAllClientsQuery), null)
        val clients = ArrayList<Client2>()
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                clients.add(
                    Client2(
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.ClientsTable.ID)),
                        cursor.getString(cursor.getColumnIndex(DBInfo2.ClientsTable.NAME)),
                        Schedule(
                            getScheduleType(cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.SCHEDULE_TYPE))),
                            cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.DAYS)),
                            cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.DURATION)),
                            ArrayList(listOf(
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.SUN)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.MON)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.TUE)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.WED)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.THU)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.FRI)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.SAT))
                            )),
                            ArrayList(listOf(
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.SUN_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.MON_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.TUE_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.WED_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.THU_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.FRI_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.SAT_DURATION))
                            ))
                        ),
                        cursor.getString(cursor.getColumnIndex(DBInfo2.ClientsTable.START_DATE)),
                        cursor.getString(cursor.getColumnIndex(DBInfo2.ClientsTable.END_DATE))
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        return clients
    }

    /**
     * Checked
     * Method to get the client type. 1 = weekly constant schedule, 2 = weekly variable session, 3 = monthly variable schedule
     * @param clientID client id value. See Clients Table
     * @return Int value representing the schedule type that a given client follows
     */
    private fun getClientType(clientID: Int): ScheduleType{
        val cursor = db.rawQuery(context.getString(R.string.getClientTypeQuery, clientID), null)
        val result: ScheduleType = if (cursor.moveToFirst()) getScheduleType(cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.SCHEDULE_TYPE)))//if record found with given client id, return corresponding ScheduleType
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
    fun checkClientConflict(client: Client2): String{
        var conflicts = ""
        val cursor = db.rawQuery(context.getString(R.string.checkClientConflictQuery, client.schedule.getCheckClientConflictDays(), client.id), null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                client.schedule.daysList.forEachIndexed { index, i ->
                    if (i > 0){
                        val strDay = StaticFunctions.NumToDay[index]
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


    fun insertClient(client: Client2): Boolean = trySQLCommand(client.getInsertCommand())
    fun updateClient(client: Client2): Boolean = trySQLCommand(client.getUpdateCommand())
    fun deleteClient(client: Client2): Boolean = trySQLCommand(client.getDeleteCommand())

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
    fun getExercise(id: Int): Exercise2 {
        //gets base Exercise class information and fills in the ExerciseType using a join and Case clause to get the appropriate data (ie if strength, muscle_name and if mobility or stability, joint_name)
        val cursor = db.rawQuery(context.getString(R.string.getExerciseQuery, id), null)
        val exercise = if (cursor.moveToFirst()) {
            val exerciseID = cursor.getInt(cursor.getColumnIndex(DBInfo2.ExercisesTable.ID))
            val primaryMover = MuscleJoint(
                cursor.getInt(cursor.getColumnIndex(DBInfo2.ExercisesTable.PRIMARY_MOVER)),
                cursor.getString(cursor.getColumnIndex(DBInfo2.AliasesUsed.PRIMARY_MOVER_NAME))
            )
            Exercise2(
                exerciseID,
                cursor.getString(cursor.getColumnIndex(DBInfo2.ExercisesTable.NAME)),
                getExerciseType(cursor.getInt(cursor.getColumnIndex(DBInfo2.ExercisesTable.TYPE))),
                primaryMover,
                getSecondaryMoversFromCSV(exerciseID, cursor.getString(cursor.getColumnIndex(DBInfo2.AliasesUsed.SECONDARY_MOVERS_IDS)), cursor.getString(cursor.getColumnIndex(DBInfo2.AliasesUsed.SECONDARY_MOVERS_NAMES)))
            )
        }
        else Exercise2(
            0,
            "",
            ExerciseType.BLANK,
            MuscleJoint(0,""),
            ArrayList()
        )
        cursor.close()
        return exercise
    }

    /**
     * Not needed
     * Private method to get many exercises using a csv String as a list. Used to populate a SessionExercise
     * @param strIDs csv string with the ids of many exercises
     * @return ArrayList of Exercise objects representing the list of exercise ids
     */
    private fun getManyExercises(strIDs: String): ArrayList<Exercise>{
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
    }

    /**
     * Checked
     * Method to get all the exercises with in the Exercises Table
     * @return ArrayList of Exercise objects representing the entire Exercise library
     */
    fun getAllExercises(): ArrayList<Exercise2>{
        //gets base Exercise class information and fills in the ExerciseType using a join and Case clause to get the appropriate data (ie if strength, muscle_name and if mobility or stability, joint_name)
        val cursor = db.rawQuery(context.getString(R.string.getAllExercisesQuery), null)
        val exercises = ArrayList<Exercise2>()
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                val exerciseID = cursor.getInt(cursor.getColumnIndex(DBInfo2.ExercisesTable.ID))
                val primaryMover = MuscleJoint(
                    cursor.getInt(cursor.getColumnIndex(DBInfo2.ExercisesTable.PRIMARY_MOVER)),
                    cursor.getString(cursor.getColumnIndex(DBInfo2.AliasesUsed.PRIMARY_MOVER_NAME))
                )
                exercises.add(
                    Exercise2(
                        exerciseID,
                        cursor.getString(cursor.getColumnIndex(DBInfo2.ExercisesTable.NAME)),
                        getExerciseType(cursor.getInt(cursor.getColumnIndex(DBInfo2.ExercisesTable.TYPE))),
                        primaryMover,
                        getSecondaryMoversFromCSV(exerciseID, cursor.getString(cursor.getColumnIndex(DBInfo2.AliasesUsed.SECONDARY_MOVERS_IDS)), cursor.getString(cursor.getColumnIndex(DBInfo2.AliasesUsed.SECONDARY_MOVERS_NAMES)))
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
        val cursor = db.rawQuery("Select exercise_name From Exercises Where exercise_name = '${exercise.name}' And exercise_id <> ${exercise.id}", null)
        val result = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun updateExercise(exercise: Exercise2): Boolean = trySQLCommand(exercise.getUpdateCommand())
    fun insertExercise(exercise: Exercise2): Boolean = trySQLCommand(exercise.getInsertCommand())
    private fun deleteExercise(exercise: Exercise2): Boolean = trySQLCommand(exercise.getDeleteCommand())

    /**
     * Checked does not remove exercise from sessions. Needs to be added back later when more changes made
     * Method used to remove an exercise from the database. Searches through sessions logged to remove references to the exercise. Places a note in the session to "preserve" the session
     * @param exercise Exercise object used to search through session log and remove exercise from database
     * @return if the removal was successful, true. if an error occurred, false
     */
    fun removeExercise(exercise: Exercise2): Boolean {
        //gets base Session class information, fills in the client_name with a join and finds only sessions containing the given exercise.id
        val cursor = db.rawQuery(context.getString(R.string.removeMuscleQuery, exercise.id), null)
        val result = if (cursor.moveToFirst()) false else deleteExercise(exercise)
        cursor.close()
        return result
    }

    /**
     * Checked
     * Method to get a Session object given a client id and daytime value (dayTime format "yyyy-MM-dd HH:mm" 24hour). Used when passing to the SessionActivity
     * @param clientID id value of the session holder
     * @param dayTime day and time in a parsable string
     * @return Session object corresponding to the given values
     */
    fun getSession(clientID: Int, dayTime: String): Session2 {
        //gets base Session class information, fills in the client_name with a join and finds only sessions for a given client and dayTime
        val cursor = db.rawQuery(context.getString(R.string.getSession_SessionQuery, clientID, dayTime), null)
        val session: Session2 = if (cursor.moveToFirst()){//if a record is found, populate the Session object
            val session = Session2(
                cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogEntry.CLIENT_ID)),
                cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.NAME)),
                cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.DAYTIME)),
                Program(
                    cursor.getInt(cursor.getColumnIndex(DBInfo2.SessionLogTable.PROGRAM_ID)),
                    cursor.getString(cursor.getColumnIndex(DBInfo2.ProgramsTable.NAME)),
                    ArrayList()
                ),
                cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.NOTES)),
                cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogEntry.DURATION))
            )
            val cursor1 = db.rawQuery(context.getString(R.string.getSession_ExerciseSessionQuery, session.getProgramID()), null)
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast){
                    val id = cursor1.getInt(cursor1.getColumnIndex(DBInfo2.ExercisesTable.ID))
                    session.addExercise(
                        ExerciseSession(
                            id,
                            cursor1.getString(cursor1.getColumnIndex(DBInfo2.ExercisesTable.NAME)),
                            getExerciseType(cursor1.getInt(cursor.getColumnIndex(DBInfo2.ExercisesTable.TYPE))),
                            MuscleJoint(
                                cursor1.getInt(cursor1.getColumnIndex(DBInfo2.ExercisesTable.PRIMARY_MOVER)),
                                cursor1.getString(cursor1.getColumnIndex(DBInfo2.AliasesUsed.PRIMARY_MOVER_NAME))
                            ),
                            getSecondaryMoversFromCSV(id, cursor1.getString(cursor.getColumnIndex(DBInfo2.AliasesUsed.SECONDARY_MOVERS_IDS)), cursor1.getString(cursor.getColumnIndex(DBInfo2.AliasesUsed.SECONDARY_MOVERS_NAMES))),
                            cursor1.getString(cursor1.getColumnIndex(DBInfo2.ProgramExercisesTable.SETS)),
                            cursor1.getString(cursor1.getColumnIndex(DBInfo2.ProgramExercisesTable.REPS)),
                            cursor1.getString(cursor1.getColumnIndex(DBInfo2.ProgramExercisesTable.RESISTANCE)),
                            cursor1.getInt(cursor1.getColumnIndex(DBInfo2.ProgramExercisesTable.EXERCISE_ORDER))
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
                Session2(
                    clientID,
                    client.name,
                    dayTime,
                    Program(
                        0,
                        "",
                        ArrayList()
                    ),
                    "",
                    0
                )
            if (checkChange(session)){//if a change for the given client and dayTime then use the corresponding duration
                val cursor1 = db.rawQuery("Select duration From Session_Changes Where client_id = $clientID And datetime(change_dayTime) = datetime('$dayTime')", null)
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
    fun checkSessionConflict(session: Session2, isSameDate: Boolean): Boolean{
        val day = getScheduleByDay(session.date)
        return day.checkConflict(session, isSameDate)
    }

    /**
     * checked
     * Method to check if a session exists in the Session_log
     * @param session Session object to be checked
     * @return true if session found in log, false if not found
     */
    fun checkSessionLog(session: Session2): Boolean{
        val cursor = db.rawQuery(context.getString(R.string.checkSessionLogQuery, session.clientID, StaticFunctions.getStrDateTime(session.date)), null)
        val result = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun insertSession(session: Session2): Boolean = trySQLCommand(session.getSQLCommand(Session.INSERT_COMMAND))
    fun updateSession(session: Session2, oldDayTime: String): Boolean = trySQLCommand(session.getSQLCommand(Session.UPDATE_COMMAND, oldDayTime))
    private fun deleteSession(session: Session2): Boolean = trySQLCommand(session.getSQLCommand(Session.DELETE_COMMAND))

    /**
     * checked
     * Method to cancel a given session. Course of action is dependent on the client's session_type. See Client Table for more information on session_type
     * @param session Session object to be canceled
     * @return true if cancellation was successful
     */
    fun cancelSession(session: Session2): Boolean{
        return when(getClientType(session.clientID)){
            ScheduleType.WEEKLY_CONSTANT ->  {
                when (true){
                    checkChange(session) -> updateChange(session, Session2(session.clientID, session.clientName, "0", Program(0,"", ArrayList()), "", session.duration))//if change record exists, update change with makeup session values
                    checkSessionLog(session) -> deleteSession(session)                                                                                                 //if session found in log, remove session from log
                    else -> insertChange(session, Session2(session.clientID, session.clientName, "0", Program(0,"", ArrayList()), "", session.duration))                //if not found anywhere insert a change with makeup session values
                }
            }
            ScheduleType.NO_SCHEDULE,ScheduleType.WEEKLY_VARIABLE,ScheduleType.MONTHLY_VARIABLE -> deleteSession(session)//delete session if not Weekly_Constant
            ScheduleType.BLANK -> false //return false, error occurred. No ScheduleType Set
        }
    }

    /**
     * checked
     * Method to get all logged sessions belonging to a client
     * @param client Client object representing the client who to be searched for logged sessions
     * @return ArrayList of Session objects representing all the sessions logged for a client
     */
    fun getClientSessions(client: Client2): ArrayList<Session2>{
        val sessions = ArrayList<Session2>()
        //gets base Session class information, fills in the client_name with a join and finds only sessions for a given client
        var cursor = db.rawQuery(context.getString(R.string.getClientSessions_SessionQuery, client.id), null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                sessions.add(
                    Session2(
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SessionLogTable.CLIENT_ID)),
                        cursor.getString(cursor.getColumnIndex(DBInfo2.ClientsTable.NAME)),
                        cursor.getString(cursor.getColumnIndex(DBInfo2.SessionLogTable.DAYTIME)),
                        Program(
                            cursor.getInt(cursor.getColumnIndex(DBInfo2.SessionLogTable.PROGRAM_ID)),
                            cursor.getString(cursor.getColumnIndex(DBInfo2.ProgramsTable.NAME)),
                            ArrayList()
                        ),
                        cursor.getString(cursor.getColumnIndex(DBInfo2.SessionLogTable.NOTES)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SessionLogTable.DURATION))
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        val builder = StringBuilder()
        sessions.forEach { if (it.getProgramID() != 0) builder.append("${it.getProgramID()},") }
        if (builder.isNotBlank())
            builder.deleteCharAt(builder.lastIndex)
        cursor = db.rawQuery(context.getString(R.string.getClientSessions_ExerciseSessionQuery, builder.toString()), null)
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast){
                val programID = cursor.getInt(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.PROGRAM_ID))
                val exerciseID = cursor.getInt(cursor.getColumnIndex(DBInfo2.ExercisesTable.ID))
                val exerciseSession = ExerciseSession(
                    exerciseID,
                    cursor.getString(cursor.getColumnIndex(DBInfo2.ExercisesTable.NAME)),
                    getExerciseType(cursor.getInt(cursor.getColumnIndex(DBInfo2.ExercisesTable.TYPE))),
                    MuscleJoint(
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.ExercisesTable.PRIMARY_MOVER)),
                        cursor.getString(cursor.getColumnIndex(DBInfo2.AliasesUsed.PRIMARY_MOVER_NAME))
                    ),
                    getSecondaryMoversFromCSV(exerciseID, cursor.getString(cursor.getColumnIndex(DBInfo2.AliasesUsed.SECONDARY_MOVERS_IDS)), cursor.getString(cursor.getColumnIndex(DBInfo2.AliasesUsed.SECONDARY_MOVERS_NAMES))),
                    cursor.getString(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.SETS)),
                    cursor.getString(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.REPS)),
                    cursor.getString(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.RESISTANCE)),
                    cursor.getInt(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.EXERCISE_ORDER))
                )
                sessions.find { it.getProgramID() == programID }.let { it?.addExercise(exerciseSession) }
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
    fun getAddSessionsClientsByDay(calendar: Calendar): ArrayList<Client2>{
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
        val clients = ArrayList<Client2>()
        val cursor = db.rawQuery(context.getString(R.string.getAddSessionsClientsByDayQuery, day.getStrIDs(), startWeek, endWeek, startMonth, endMonth), null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                clients.add(Client2(
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.ClientsTable.ID)),
                        cursor.getString(cursor.getColumnIndex(DBInfo2.ClientsTable.NAME)),
                        Schedule(
                            getScheduleType(cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.SCHEDULE_TYPE))),
                            cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.DAYS)),
                            cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.DURATION)),
                            ArrayList(listOf(
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.SUN)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.MON)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.TUE)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.WED)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.THU)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.FRI)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.SAT))
                            )),
                            ArrayList(listOf(
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.SUN_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.MON_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.TUE_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.WED_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.THU_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.FRI_DURATION)),
                                cursor.getInt(cursor.getColumnIndex(DBInfo2.SchedulesTable.SAT_DURATION))
                            ))
                        ),
                        cursor.getString(cursor.getColumnIndex(DBInfo2.ClientsTable.START_DATE)),
                        cursor.getString(cursor.getColumnIndex(DBInfo2.ClientsTable.END_DATE))
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
    fun getScheduleByDay(calendar: Calendar): Day2 {
        val sessions = ArrayList<Session2>()
        val calendarNow = Calendar.getInstance()
        val calendarChosen = Calendar.getInstance()
        calendarChosen.time = calendar.time
        val currentDate = (calendarNow[Calendar.YEAR] * 365) + calendarNow[Calendar.DAY_OF_YEAR]
        val chosenDate = (calendarChosen[Calendar.YEAR] * 365) + calendarChosen[Calendar.DAY_OF_YEAR]

        val date = StaticFunctions.getStrDateTime(calendarChosen)
        var cursor = db.rawQuery(context.getString(R.string.getScheduleByDay_SessionLogQuery, date), null)
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast){
                sessions.add(
                    Session2(
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SessionLogTable.CLIENT_ID)),
                        cursor.getString(cursor.getColumnIndex(DBInfo2.ClientsTable.NAME)),
                        cursor.getString(cursor.getColumnIndex(DBInfo2.SessionLogTable.DAYTIME)),
                        Program(
                            cursor.getInt(cursor.getColumnIndex(DBInfo2.SessionLogTable.PROGRAM_ID)),
                            cursor.getString(cursor.getColumnIndex(DBInfo2.ProgramsTable.NAME)),
                            ArrayList()
                        ),
                        cursor.getString(cursor.getColumnIndex(DBInfo2.SessionLogTable.NOTES)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.SessionLogTable.DURATION))
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        val builder = StringBuilder()
        sessions.forEach { if (it.getProgramID() != 0) builder.append("${it.getProgramID()},") }
        if (builder.isNotBlank())
            builder.deleteCharAt(builder.lastIndex)
        cursor = db.rawQuery(context.getString(R.string.getClientSessions_ExerciseSessionQuery, builder.toString()), null)
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast){
                val programID = cursor.getInt(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.PROGRAM_ID))
                val exerciseID = cursor.getInt(cursor.getColumnIndex(DBInfo2.ExercisesTable.ID))
                val exerciseSession = ExerciseSession(
                    exerciseID,
                    cursor.getString(cursor.getColumnIndex(DBInfo2.ExercisesTable.NAME)),
                    getExerciseType(cursor.getInt(cursor.getColumnIndex(DBInfo2.ExercisesTable.TYPE))),
                    MuscleJoint(
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.ExercisesTable.PRIMARY_MOVER)),
                        cursor.getString(cursor.getColumnIndex(DBInfo2.AliasesUsed.PRIMARY_MOVER_NAME))
                    ),
                    getSecondaryMoversFromCSV(exerciseID, cursor.getString(cursor.getColumnIndex(DBInfo2.AliasesUsed.SECONDARY_MOVERS_IDS)), cursor.getString(cursor.getColumnIndex(DBInfo2.AliasesUsed.SECONDARY_MOVERS_NAMES))),
                    cursor.getString(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.SETS)),
                    cursor.getString(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.REPS)),
                    cursor.getString(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.RESISTANCE)),
                    cursor.getInt(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.EXERCISE_ORDER))
                )
                sessions.find { it.getProgramID() == programID }.let { it?.addExercise(exerciseSession) }
                cursor.moveToNext()
            }
        }
        if (chosenDate >= currentDate) {
            val dayOfWeek = StaticFunctions.NumToDay[calendarChosen[Calendar.DAY_OF_WEEK]]
            cursor = db.rawQuery(context.getString(R.string.getScheduleByDay_ClientsQuery, date, dayOfWeek), null)
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast){
                    val time = cursor.getInt(cursor.getColumnIndex(dayOfWeek))
                    val duration = cursor.getInt(cursor.getColumnIndex("${dayOfWeek}_duration"))
                    val minutes = time % 60
                    val hour = (time - minutes) / 60
                    calendarChosen[Calendar.HOUR_OF_DAY] = hour
                    calendarChosen[Calendar.MINUTE] = minutes
                    sessions.add(
                        Session2(
                            cursor.getInt(cursor.getColumnIndex(DBInfo2.ClientsTable.ID)),
                            cursor.getString(cursor.getColumnIndex(DBInfo2.ClientsTable.NAME)),
                            StaticFunctions.getStrDateTime(calendarChosen),
                            Program(
                                0,
                                "",
                                ArrayList()
                            ),
                            "",
                            duration
                        )
                    )
                    cursor.moveToNext()
                }
            }
            cursor.close()
            cursor = db.rawQuery(context.getString(R.string.getScheduleByDay_SessionChangesQuery, date), null)
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast){
                    sessions.add(
                        Session2(
                            cursor.getInt(cursor.getColumnIndex(DBInfo2.SessionChangesTable.CLIENT_ID)),
                            cursor.getString(cursor.getColumnIndex(DBInfo2.ClientsTable.NAME)),
                            cursor.getString(cursor.getColumnIndex(DBInfo2.SessionChangesTable.CHANGE_DAYTIME)),
                            Program(
                                0,
                                "",
                                ArrayList()
                            ),
                            "",
                            cursor.getInt(cursor.getColumnIndex(DBInfo.SessionChangesEntry.DURATION))
                        )
                    )
                    cursor.moveToNext()
                }
            }
            cursor.close()
        }

        return Day2(sessions)
    }

    /**
     * checked
     * Method to check if a session exists in the Session_Changes Table
     * @param session Session object containing the information about a session
     * @return true if session exists
     */
    fun checkChange(session: Session2): Boolean{
        val cursor = db.rawQuery(context.getString(R.string.checkChangeQuery, session.clientID, StaticFunctions.getStrDateTime(session.date)), null)
        val result: Boolean = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun insertChange(oldSession: Session2, newSession: Session2): Boolean = trySQLCommand(context.getString(R.string.insertChangeCommand,
                                                                                                            oldSession.clientID,
                                                                                                            StaticFunctions.getStrDateTime(oldSession.date),
                                                                                                            StaticFunctions.getStrDateTime(newSession.date),
                                                                                                            newSession.duration))
    fun updateChange(oldSession: Session2, newSession: Session2): Boolean = trySQLCommand(context.getString(R.string.updateChangeCommand,
                                                                                                            StaticFunctions.getStrDateTime(newSession.date),
                                                                                                            newSession.duration,
                                                                                                            oldSession.clientID))
    private fun deleteChange(session: Session2): Boolean = trySQLCommand(context.getString(R.string.deleteChangeCommand, session.clientID, StaticFunctions.getStrDateTime(session.date)))

    /**
     * checked
     * Method to get the most recent values of an exercise that was completed by a given client
     * @param exercise Exercise object used to get the exercise id and to populate the ExerciseSession object
     * @param clientID id of the given client used to search Session_log table
     * @return ExerciseSession object containing the information about the last occurrence of the given exercise. All non-Exercise object fields are blank if the client has not already performed the exercise
     */
    fun getLastOccurrence(exercise: Exercise, clientID: Int): ExerciseSession {
        val cursor = db.rawQuery(context.getString(R.string.getLastOccurrenceQuery, clientID, exercise.id), null)
        val exerciseSession: ExerciseSession = if (cursor.moveToFirst()){
            ExerciseSession(
                exercise,
                cursor.getString(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.SETS)),
                cursor.getString(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.REPS)),
                cursor.getString(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.RESISTANCE)),
                cursor.getInt(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.EXERCISE_ORDER))
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
        val cursor = db.rawQuery(context.getString(R.string.getAllOccurrencesQuery, clientID, exercise.id), null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                exerciseSessions.add(
                    ExerciseSession(
                        exercise,
                        cursor.getString(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.SETS)),
                        cursor.getString(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.REPS)),
                        cursor.getString(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.RESISTANCE)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo2.ProgramExercisesTable.EXERCISE_ORDER))
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        return exerciseSessions
    }
}