package com.trainingapp.personaltrainingassistant.database

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.trainingapp.personaltrainingassistant.StaticFunctions
import com.trainingapp.personaltrainingassistant.enumerators.ExerciseType
import com.trainingapp.personaltrainingassistant.enumerators.ScheduleType
import com.trainingapp.personaltrainingassistant.objects.*
import java.util.*
import kotlin.collections.ArrayList

class DatabaseOperations(context: Context) {

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
     * Private method to get the list of secondary movers as an ArrayList of MuscleJoint data objects
     * @param id id of the exercise obtained from the database query
     * @param strSecondaryMovers csv String file extracted from the database query
     * @param exerciseType ExerciseType enum value associated with the exercise found from the database query
     * @return ArrayList of MuscleJoint data objects corresponding to the movers csv sent as a parameter
     */
    private fun getSecondaryMovers(id: Int, strSecondaryMovers: String, exerciseType: ExerciseType): ArrayList<MuscleJoint>{
        val secondaryMovers = ArrayList<MuscleJoint>()
        if (id == 0 || strSecondaryMovers == ""){//if the id = 0 that means no exercise was found. if strSecondaryMovers is empty than no secondary movers are present. Either way, return a blank ArrayList is returned
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

    /**
     * Method to get the user settings from the database
     * @return ArrayList of the user settings as Ints
     */
    fun getUserSettings(): ArrayList<Int>{
        val cursor = db.rawQuery("Select default_duration, '24_clock' From User_Settings", null)
        val result = if (cursor.moveToFirst()) ArrayList<Int>(arrayOf(
            cursor.getInt(cursor.getColumnIndex(DBInfo.UserSettingsEntry.DEFAULT_DURATION)),
            cursor.getInt(cursor.getColumnIndex(DBInfo.UserSettingsEntry.CLOCK_24))
        ).asList())
        else
            ArrayList()
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
        val cursor = db.rawQuery("Select joint_id, joint_name From Joints Where joint_id = $id", null)
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
     * Method to get all the joints found in the database
     * @return ArrayList of MuscleJoint data objects for all the joints defined
     */
    fun getAllJoints(): ArrayList<MuscleJoint>{
        val joints = ArrayList<MuscleJoint>()
        val cursor = db.rawQuery("Select joint_id, joint_name From Joints", null)
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
     * Method to get a single muscle from the Muscles Table based upon a given id
     * @param id id of the muscle to be found
     * @return MuscleJoint data object representing the desired muscle
     */
    fun getMuscle(id: Int): MuscleJoint {
        val cursor = db.rawQuery("Select muscle_id, muscle_name From Muscles Where muscle_id = $id", null)
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
     * Method to get all the muscles from the Muscles Table
     * @return ArrayList of MuscleJoint data objects representing all the defined muscles
     */
    fun getAllMuscles(): ArrayList<MuscleJoint>{
        val muscles = ArrayList<MuscleJoint>()
        val cursor = db.rawQuery("Select muscle_id, muscle_name From Muscles Order By muscle_name", null)
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
     * Method used to remove a muscle from the database. Will not remove a muscle if the muscle is used to define an exercise (ie defines the primary_mover or is a secondary_mover)
     * @param muscleJoint muscle data class containing the id and name of the muscle to be removed
     * @return if the muscle defines an exercise 2, if the deletion is handled with no error 1, if the deletion has an error 0
     */
    fun removeMuscle(muscleJoint: MuscleJoint): Int{
        val cursor = db.rawQuery("Select * From Exercises Where exercise_type = 1 And (primary_mover = ${muscleJoint.id} Or secondary_movers Like '${muscleJoint.id},%' Or secondary_movers Like '%,${muscleJoint.id},%' Or secondary_movers Like '%,${muscleJoint.id}' or secondary_movers = '${muscleJoint.id}')", null)
        val result =  if (cursor.moveToFirst())
            2
        else {
            if (deleteMuscle(muscleJoint)) 1 else 0
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
        val cursor = db.rawQuery("Select muscle_name From Muscles Where muscle_name = '${muscle.name}'", null)
        val result = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun addMuscle(muscleJoint: MuscleJoint): Boolean = trySQLCommand("Insert Into Muscles(muscle_name) Values('${muscleJoint.name}')")
    fun updateMuscle(muscleJoint: MuscleJoint): Boolean = trySQLCommand("Update Muscles Set muscle_name = '${muscleJoint.name}' Where muscle_id = ${muscleJoint.id}")
    private fun deleteMuscle(muscleJoint: MuscleJoint): Boolean = trySQLCommand("Delete From Muscles Where muscle_id = ${muscleJoint.id}")

    /**
     * Method to get a Client object given a client id by searching for that client's information. If a client is not found with the given id, a blank client is returned
     * @param id id of the client to be found
     * @return Client object for the given client id
     */
    fun getClient(id: Int): Client {
        val cursor = db.rawQuery("Select * From Clients Where client_id = $id", null)
        val client: Client = if (cursor.moveToFirst())//if a record is found, return a populated Client object
            Client(
                cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsEntry.ID)),
                cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.NAME)),
                getScheduleType(cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsEntry.SCHEDULE_TYPE))),
                cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.DAYS)),
                cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.TIMES)),
                cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.DURATIONS)),
                cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.START_DATE)),
                cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.END_DATE))
            )
        else//if no client found with given id, return an empty object
            Client(
                0,
                "",
                ScheduleType.NO_SCHEDULE,
                "",
                "",
                "",
                "",
                ""
            )
        cursor.close()
        return client
    }

    /**
     * Method to get all clients currently in the Clients Table
     * @return ArrayList of Client objects for all defined clients
     */
    fun getAllClients(): ArrayList<Client>{
        val cursor = db.rawQuery("Select * From Clients Order By client_name", null)
        val clients = ArrayList<Client>()
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                clients.add(
                    Client(
                        cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsEntry.ID)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.NAME)),
                        getScheduleType(cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsEntry.SCHEDULE_TYPE))),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.DAYS)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.TIMES)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.DURATIONS)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.START_DATE)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.END_DATE))
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
        val cursor = db.rawQuery("Select schedule_type From Clients Where client_id = $clientID", null)
        val result: ScheduleType = if (cursor.moveToFirst()) getScheduleType(cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsEntry.SCHEDULE_TYPE)))//if record found with given client id, return corresponding ScheduleType
        else ScheduleType.BLANK//if no record found, return BLANK or -1
        cursor.close()
        return result
    }

    /**
     * Method to check if a client conflicts with the given client list. Only looks at clients with constant schedules (ie session_type = 1)
     * Should only be called if the client's ScheduleType is Weekly_Constant
     * @param client Client object created from data collected in the CreateOrAlterClientActivity
     * @return empty string if a conflict is found with the current constant schedule. String with conflict client names if any conflicts are found
     */
    fun checkClientConflict(client: Client): String{
        var conflicts = ""
        for(index in client.days.indices) {//for each day that the client is coming in, check all clients who that already train on that day, who's start and end date are outside of today and who's ScheduleType is Weekly_Constant
            val day = client.days[index]
            val cursor = db.rawQuery("Select times, durations, client_name, days From Clients Where days Like '%$day%' And start_date <= date('now') And end_date >= date('now') And schedule_type = 1", null)
            if (cursor.moveToFirst()) {
                while(!cursor.isAfterLast) {
                    val position = cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.DAYS)).split(",").indexOf(day.toString())//find the index of the pertinent day and use that to get the time and duration for the possibly overlapping session
                    val time = cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.TIMES)).split(",")[position].toInt()//find the start time of the day in question
                    val duration = cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.DURATIONS)).split(",")[position].toInt()//find the duration of the day in questions
                    val upper = time + duration//find the upper limit of the session
                    if (StaticFunctions.compareTimeRanges(time..upper,client.times[index] until (client.times[index] + client.durations[index])))//compare the time range of the found client with the new/updated client
                        conflicts += "${cursor.getString(2)},"//if there is overlap, add the existing client's name to the conflicts string
                    cursor.moveToNext()
                }
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
     * Method to get an Exercises object given and exercise id
     * @param id id value of an exercise. See Exercises Table for more information
     * @return Exercise object for the given id value
     */
    fun getExercise(id: Int): Exercise {
        //gets base Exercise class information and fills in the ExerciseType using a join and Case clause to get the appropriate data (ie if strength, muscle_name and if mobility or stability, joint_name)
        val cursor = db.rawQuery("Select e.exercise_id, e.exercise_name, e.exercise_type, e.primary_mover, e.secondary_movers, Case When e.exercise_type = 1 then m.muscle_name When e.exercise_type = 2 or e.exercise_type = 3 then j.joint_name End From Exercises e inner join Exercise_Types t on e.exercise_type=t.exercise_type_id left join Muscles m on e.primary_mover=m.muscle_id left join Joints j on e.primary_mover=j.joint_id Where e.exercise_id = $id", null)
        val exercise = if (cursor.moveToFirst()) {
            val exerciseID = cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesEntry.ID))
            val strSecondaryMovers = cursor.getString(cursor.getColumnIndex(DBInfo.ExercisesEntry.SECONDARY_MOVERS))
            val exerciseType = getExerciseType(cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesEntry.TYPE)))
            val primaryMover = MuscleJoint(cursor.getInt(cursor.getColumnIndex(DBInfo.ExercisesEntry.PRIMARY_MOVER)), cursor.getString(5))
            Exercise(
                exerciseID,
                cursor.getString(cursor.getColumnIndex(DBInfo.ExercisesEntry.NAME)),
                exerciseType,
                primaryMover,
                getSecondaryMovers(exerciseID,strSecondaryMovers,exerciseType)
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

    /**
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
     * Method to get all the exercises with in the Exercises Table
     * @return ArrayList of Exercise objects representing the entire Exercise library
     */
    fun getAllExercises(): ArrayList<Exercise>{
        //gets base Exercise class information and fills in the ExerciseType using a join and Case clause to get the appropriate data (ie if strength, muscle_name and if mobility or stability, joint_name)
        val cursor = db.rawQuery("Select e.exercise_id, e.exercise_name, e.exercise_type, e.primary_mover, e.secondary_movers, Case When e.exercise_type = 1 then m.muscle_name When e.exercise_type = 2 or e.exercise_type = 3 then j.joint_name End From Exercises e inner join Exercise_Types t on e.exercise_type=t.exercise_type_id left join Muscles m on e.primary_mover=m.muscle_id left join Joints j on e.primary_mover=j.joint_id Order By e.exercise_name", null)
        val exercises = ArrayList<Exercise>()
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
     * Method to check if a current exercise name is already in the Exercises Table
     * @param exercise Exercise object collected from the user in CreateOrAlterExerciseActivity
     * @return true if a conflict is found
     */
    fun checkExerciseConflict(exercise: Exercise): Boolean{
        val cursor = db.rawQuery("Select exercise_name From Exercises Where exercise_name = '${exercise.name}'", null)
        val result = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun updateExercise(exercise: Exercise): Boolean = trySQLCommand(exercise.getUpdateCommand())
    fun insertExercise(exercise: Exercise): Boolean = trySQLCommand(exercise.getInsertCommand())
    private fun deleteExercise(exercise: Exercise): Boolean = trySQLCommand(exercise.getDeleteCommand())

    /**
     * Method used to remove an exercise from the database. Searches through sessions logged to remove references to the exercise. Places a note in the session to "preserve" the session
     * @param exercise Exercise object used to search through session log and remove exercise from database
     * @return if the removal was successful, true. if an error occurred, false
     */
    fun removeExercise(exercise: Exercise): Boolean {
        val sessions = ArrayList<Session>()
        //gets base Session class information, fills in the client_name with a join and finds only sessions containing the given exercise.id
        val cursor = db.rawQuery("Select s.client_id, s.dayTime, s.exercise_ids, s.sets, s.reps, s.resistances, s.exercise_order, s.notes, s.duration, c.client_name From Session_log s Inner Join Clients c On s.client_id=c.client_id where (exercise_ids Like '${exercise.id},%' Or exercise_ids Like '%,${exercise.id},%' Or exercise_ids Like '%,${exercise.id}' or exercise_ids = '${exercise.id}')", null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                sessions.add(Session(
                    cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogEntry.CLIENT_ID)),
                    cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.NAME)),
                    cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.DAYTIME)),
                    getListExerciseSessions(
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.EXERCISE_IDS)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.SETS)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.REPS)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.RESISTANCE)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.EXERCISE_ORDER))
                    ),
                    cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.NOTES)),
                    cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogEntry.DURATION))
                ))
                //find the ExerciseSession object for the most recently inserted session. Uses that object to remove itself from the session and add a removal note to the notes section
                val exerciseSession = sessions[sessions.lastIndex].getExercise(exercise)
                sessions[sessions.lastIndex].removeExercise(exercise)
                sessions[sessions.lastIndex].notes += "*removed:${exerciseSession.name} sets:${exerciseSession.sets} reps:${exerciseSession.reps} resistance:${exerciseSession.resistance}"
            }
        }
        cursor.close()
        sessions.forEach { updateSession(it, "") }//updates all the found session with the new values
        return deleteExercise(exercise)
    }

    /**
     * Method to get a Session object given a client id and daytime value (dayTime format "yyyy-MM-dd HH:mm" 24hour). Used when passing to the SessionActivity
     * @param clientID id value of the session holder
     * @param dayTime day and time in a parsable string
     * @return Session object corresponding to the given values
     */
    fun getSession(clientID: Int, dayTime: String): Session {
        //gets base Session class information, fills in the client_name with a join and finds only sessions for a given client and dayTime
        val cursor = db.rawQuery("Select s.client_id, s.dayTime, s.exercise_ids, s.sets, s.reps, s.resistances, s.exercise_order, s.notes, s.duration, c.client_name From Session_log s Inner Join Clients c On s.client_id=c.client_id Where s.client_id = $clientID And date(dayTime) = date('$dayTime')", null)
        val session: Session = if (cursor.moveToFirst()){//if a record is found, populate the Session object
            Session(
                cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogEntry.CLIENT_ID)),
                cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.NAME)),
                cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.DAYTIME)),
                getListExerciseSessions(
                    cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.EXERCISE_IDS)),
                    cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.SETS)),
                    cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.REPS)),
                    cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.RESISTANCE)),
                    cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.EXERCISE_ORDER))
                ),
                cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.NOTES)),
                cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogEntry.DURATION))
            )
        } else{//if a logged session is not found, create a blank session then fill in the duration using the logic below
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

    //removes the first available makeup session (represented with a 0 in change_dayTime)
    fun removeCanceledSession(clientID: Int): Boolean = trySQLCommand("Delete Top(1) From Session_Changes Where client_id = $clientID And change_dayTime = '0'")

    /**
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
     * Method to check if a session exists in the Session_log
     * @param session Session object to be checked
     * @return true if session found in log, false if not found
     */
    fun checkSessionLog(session: Session): Boolean{
        val cursor = db.rawQuery("Select client_id From Session_log s Where s.client_id = ${session.clientID} And date(dayTime) = date('${StaticFunctions.getStrDateTime(session.date)}')", null)
        val result = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun insertSession(session: Session): Boolean = trySQLCommand(session.getSQLCommand(Session.INSERT_COMMAND))
    fun updateSession(session: Session, oldDayTime: String): Boolean = trySQLCommand(session.getSQLCommand(Session.UPDATE_COMMAND, oldDayTime))
    private fun deleteSession(session: Session): Boolean = trySQLCommand(session.getSQLCommand(Session.DELETE_COMMAND))

    /**
     * Method to cancel a given session. Course of action is dependent on the client's session_type. See Client Table for more information on session_type
     * @param session Session object to be canceled
     * @return true if cancellation was successful
     */
    fun cancelSession(session: Session): Boolean{
        return when(getClientType(session.clientID)){
            ScheduleType.WEEKLY_CONSTANT ->  {
                when (true){
                    checkChange(session) -> updateChange(session, Session(session.clientID, session.clientName, "0", ArrayList(), "", session.duration))//if change record exists, update change with makeup session values
                    checkSessionLog(session) -> deleteSession(session)                                                                                                 //if session found in log, remove session from log
                    else -> insertChange(session, Session(session.clientID, session.clientName, "0", ArrayList(), "", session.duration))                //if not found anywhere insert a change with makeup session values
                }
            }
            ScheduleType.NO_SCHEDULE,ScheduleType.WEEKLY_VARIABLE,ScheduleType.MONTHLY_VARIABLE -> deleteSession(session)//delete session if not Weekly_Constant
            ScheduleType.BLANK -> false //return false, error occurred. No ScheduleType Set
        }
    }

    /**
     * Method to get all logged sessions belonging to a client
     * @param client Client object representing the client who to be searched for logged sessions
     * @return ArrayList of Session objects representing all the sessions logged for a client
     */
    fun getClientSessions(client: Client): ArrayList<Session>{
        val sessions = ArrayList<Session>()
        //gets base Session class information, fills in the client_name with a join and finds only sessions for a given client
        val cursor = db.rawQuery("Select s.client_id, s.dayTime, s.exercise_ids, s.sets, s.reps, s.resistances, s.exercise_order, s.notes, s.duration, c.client_name From Session_log s Inner Join Clients c On s.client_id=c.client_id Where s.client_id = ${client.id}", null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                sessions.add(
                    Session(
                        cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogEntry.CLIENT_ID)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.NAME)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.DAYTIME)),
                        getListExerciseSessions(
                            cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.EXERCISE_IDS)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.SETS)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.REPS)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.RESISTANCE)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.EXERCISE_ORDER))
                        ),
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.NOTES)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogEntry.DURATION))
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        return sessions
    }

    /**
     *
     */
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
        val cursor = db.rawQuery("Select * From Clients c Where c.client_id Not in (${day.getStrIDs()}) And (c.schedule_type = 0 Or (c.schedule_type = 1 And (Select sc.client_id From Session_Changes sc Where sc.client_id = c.client_id And sc.change_dayTime = '0') > 0) Or (c.schedule_type = 2 And (Select count(sl.client_id) From Session_log sl Where sl.client_id = c.client_id And date(sl.dayTime) >= date('${startWeek}') And date(sl.dayTime) <= date('${endWeek}')) < c.days) Or (c.schedule_type = 3 And (Select count(sl.client_id) From Session_log sl Where sl.client_id = c.client_id And date(sl.dayTime) >= date('${startMonth}') And date(sl.dayTime) <= date('${endMonth}')) < c.days))", null)
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast){
                clients.add(Client(
                    cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsEntry.ID)),
                    cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.NAME)),
                    getScheduleType(cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsEntry.SCHEDULE_TYPE))),
                    cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.DAYS)),
                    cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.TIMES)),
                    cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.DURATIONS)),
                    cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.START_DATE)),
                    cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.END_DATE))
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

        val date = StaticFunctions.getStrDateTime(calendarChosen)
        var cursor = db.rawQuery("Select s.client_id, s.dayTime, s.exercise_ids, s.sets, s.reps, s.resistances, s.exercise_order, s.notes, s.duration, c.client_name From Session_log s Inner Join Clients c On s.client_id=c.client_id Where date(dayTime) = date('$date')", null)
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast){
                sessions.add(
                    Session(
                        cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogEntry.CLIENT_ID)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.NAME)),
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.DAYTIME)),
                        getListExerciseSessions(
                            cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.EXERCISE_IDS)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.SETS)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.REPS)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.RESISTANCE)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.EXERCISE_ORDER))
                        ),
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.NOTES)),
                        cursor.getInt(cursor.getColumnIndex(DBInfo.SessionLogEntry.DURATION))
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        return if (chosenDate < currentDate) Day(sessions)
        else {
            val dayOfWeek = calendarChosen[Calendar.DAY_OF_WEEK]
            cursor = db.rawQuery("Select c.client_id, c.client_name, c.times, c.days, c.durations From Clients c Where days Like '%$dayOfWeek%' And date(end_date) >= date('$date') And date(start_date) <= date('$date') And schedule_type = 1 And Not (c.client_id in (Select Distinct client_id From Session_Changes Where date(normal_dayTime) = date('$date')) Or c.client_id in (Select s.client_id From Session_log s Where date(dayTime) = date('$date')))", null)
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast){
                    val index = cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.DAYS)).split(",").indexOf(dayOfWeek.toString())
                    val time = StaticFunctions.toArrayListInt(cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.TIMES)))[index]
                    val duration = StaticFunctions.toArrayListInt(cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.DURATIONS)))[index]
                    val minutes = time % 60
                    val hour = (time - minutes) / 60
                    calendarChosen[Calendar.HOUR_OF_DAY] = hour
                    calendarChosen[Calendar.MINUTE] = minutes
                    sessions.add(
                        Session(
                            cursor.getInt(cursor.getColumnIndex(DBInfo.ClientsEntry.ID)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.NAME)),
                            StaticFunctions.getStrDateTime(calendarChosen),
                            ArrayList(),
                            "",
                            duration
                        )
                    )
                    cursor.moveToNext()
                }
            }
            cursor.close()
            cursor = db.rawQuery("Select sc.client_id, c.client_name, sc.change_dayTime, sc.duration From Session_Changes sc Inner Join Clients c On c.client_id=sc.client_id where date(sc.change_dayTime) = date('$date')  And Not sc.client_id in (Select s.client_id From Session_log s Where date(dayTime) = date('$date'))", null)
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast){
                    sessions.add(
                        Session(
                            cursor.getInt(cursor.getColumnIndex(DBInfo.SessionChangesEntry.CLIENT_ID)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.ClientsEntry.NAME)),
                            cursor.getString(cursor.getColumnIndex(DBInfo.SessionChangesEntry.CHANGE_DAYTIME)),
                            ArrayList(),
                            "",
                            cursor.getInt(cursor.getColumnIndex(DBInfo.SessionChangesEntry.DURATION))
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
            val index = cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.EXERCISE_IDS)).split(",").indexOf(exercise.id.toString())
            ExerciseSession(
                exercise,
                cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.SETS)).split(",")[index],
                cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.REPS)).split(",")[index],
                cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.RESISTANCE)).split(",")[index],
                cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.EXERCISE_ORDER)).split(",")[index].toInt()
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
                val index = cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.EXERCISE_IDS)).split(",").indexOf(exercise.id.toString())
                exerciseSessions.add(
                    ExerciseSession(
                        exercise,
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.SETS)).split(",")[index],
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.REPS)).split(",")[index],
                        cursor.getString(cursor.getColumnIndex(DBInfo.SessionLogEntry.RESISTANCE)).split(",")[index],
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