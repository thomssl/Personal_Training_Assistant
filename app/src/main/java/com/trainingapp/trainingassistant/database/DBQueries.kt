package com.trainingapp.trainingassistant.database

object DBQueries {

    class DBOperations {
        companion object{
            fun getExercise(id: Int) =  "Select e.exercise_id," +
                                "               e.exercise_name," +
                                "               e.exercise_type," +
                                "               e.primary_mover_id," +
                                "               (" +
                                "                    Case" +
                                "                        When e.exercise_type = 1 then m.muscle_name" +
                                "                        When e.exercise_type = 2 or e.exercise_type = 3 then j.joint_name" +
                                "                    End" +
                                "                ) as 'primary_mover_name'," +
                                "               ifnull" +
                                "                (" +
                                "                    (" +
                                "                        Select group_concat(s.secondary_mover_id)" +
                                "                        From secondary_movers s" +
                                "                        Where s.exercise_id = $id" +
                                "                        Group By s.exercise_id" +
                                "                    )," +
                                "                    '0'" +
                                "                ) as 'secondary_movers_ids'," +
                                "               ifnull" +
                                "                (" +
                                "                    (" +
                                "                        Select group_concat" +
                                "                        (" +
                                "                            (" +
                                "                                Case" +
                                "                                    When e.exercise_type = 1 then m.muscle_name" +
                                "                                    When e.exercise_type = 2 or e.exercise_type = 3 then j.joint_name" +
                                "                                End" +
                                "                            )" +
                                "                        )" +
                                "                        From secondary_movers s" +
                                "                        left join Muscles m on s.secondary_mover_id=m.muscle_id " +
                                "                        left join Joints j on s.secondary_mover_id=j.joint_id" +
                                "                        Where s.exercise_id = $id" +
                                "                        Group By s.exercise_id" +
                                "                    )," +
                                "                    '0'" +
                                "                ) as 'secondary_movers_names'" +
                                "        From Exercises e" +
                                "        left join Muscles m on e.primary_mover_id=m.muscle_id" +
                                "        left join Joints j on e.primary_mover_id=j.joint_id" +
                                "        Where e.exercise_id = $id;"

            fun getAllExercises() = "Select e.exercise_id," +
                            "               e.exercise_name," +
                            "               e.exercise_type," +
                            "               e.primary_mover_id," +
                            "               (" +
                            "                    Case" +
                            "                        When e.exercise_type = 1 then m.muscle_name" +
                            "                        When e.exercise_type = 2 or e.exercise_type = 3 then j.joint_name" +
                            "                    End" +
                            "                ) as 'primary_mover_name'," +
                            "               ifnull" +
                            "                (" +
                            "                    (" +
                            "                        Select group_concat(s.secondary_mover_id)" +
                            "                        From secondary_movers s" +
                            "                        Where s.exercise_id = e.exercise_id" +
                            "                        Group By s.exercise_id" +
                            "                    )," +
                            "                    '0'" +
                            "                ) as 'secondary_movers_ids'," +
                            "               ifnull" +
                            "                (" +
                            "                    (" +
                            "                        Select group_concat" +
                            "                        (" +
                            "                            (" +
                            "                                Case" +
                            "                                    When e.exercise_type = 1 then m.muscle_name" +
                            "                                    When e.exercise_type = 2 or e.exercise_type = 3 then j.joint_name" +
                            "                                End" +
                            "                            )" +
                            "                        )" +
                            "                        From secondary_movers s" +
                            "                        left join Muscles m on s.secondary_mover_id=m.muscle_id" +
                            "                        left join Joints j on s.secondary_mover_id=j.joint_id" +
                            "                        Where s.exercise_id = e.exercise_id" +
                            "                        Group By s.exercise_id" +
                            "                    )," +
                            "                    '0'" +
                            "                ) as 'secondary_movers_names'" +
                            "        From Exercises e" +
                            "        left join Muscles m on e.primary_mover_id=m.muscle_id" +
                            "        left join Joints j on e.primary_mover_id=j.joint_id;"

            fun getExerciseUsage(id: Int) = "Select *" +
                                    "        From (Select *" +
                                    "              From Program_Exercises pe" +
                                    "              Where pe.exercise_id = $id" +
                                    "              Union All" +
                                    "              Select *" +
                                    "              From Session_Exercises se" +
                                    "              Where se.exercise_id = $id" +
                                    "             );"

            fun getExerciseConflict(name: String, id: Int) =    "Select exercise_name" +
                                                        "        From Exercises" +
                                                        "        Where exercise_name = '$name'" +
                                                        "        And exercise_id <> $id;"

            fun getMuscle(id: Int) =    "Select muscle_id," +
                                "               muscle_name" +
                                "        From Muscles" +
                                "        Where muscle_id = $id;"

            fun getAllMuscles() =   "Select muscle_id," +
                            "               muscle_name" +
                            "        From Muscles" +
                            "        Order By muscle_name;"

            fun getMuscleUsage(id: Int) =   "Select DISTINCT e.exercise_id" +
                                    "        From Exercises e" +
                                    "        Inner Join secondary_movers s on s.exercise_id=e.exercise_id" +
                                    "        Where e.exercise_type = 1" +
                                    "        And (e.primary_mover_id = $id or s.secondary_mover_id = $id);"

            fun getMuscleConflict(name: String, id: Int) =  "Select muscle_name" +
                                                    "        From Muscles" +
                                                    "        Where muscle_name = '$name'" +
                                                    "        And muscle_id <> $id"

            fun insertMuscle(name: String) =    "Insert Into Muscles(muscle_name)" +
                                        "        Values('$name')"

            fun updateMuscle(name: String, id: Int) =   "Update Muscles" +
                                                "        Set muscle_name = '$name'" +
                                                "        Where muscle_id = $id"

            fun deleteMuscle(id: Int) = "Delete From Muscles" +
                                "        Where muscle_id = $id"

            fun getJoint(id: Int) = "Select joint_id," +
                            "               joint_name" +
                            "        From Joints" +
                            "        Where joint_id = $id;"

            fun getAllJoints() =    "Select joint_id," +
                            "               joint_name" +
                            "        From Joints;"

            fun getClient(id: Int) =    "Select *" +
                                "        From Clients c" +
                                "        Where c.client_id = $id;"

            fun getAllClients() =   "Select *" +
                            "        From Clients c;"

            fun getClientType(id: Int) =    "Select schedule_type" +
                                    "        From Schedules" +
                                    "        Where client_id = $id;"

            fun addClientBank(id: Int) =    "Update Clients" +
                                    "        Set banked_sessions = banked_sessions + 1" +
                                    "        Where client_id = $id;"

            fun decClientBank(id: Int) =    "Update Clients" +
                                    "        Set banked_sessions = banked_sessions - 1" +
                                    "        Where client_id = $id;"

            fun getClientConflict(conflictDays: String, id: Int) =  "Select *" +
                                                            "        From Clients c" +
                                                            "        Where $conflictDays" +
                                                            "        c.start_date <= date('now')" +
                                                            "        And c.end_date >= date('now')" +
                                                            "        And c.schedule_type = 1" +
                                                            "        And c.client_id <> $id;"

            fun getClientSessions(id: Int) =    "Select s.client_id," +
                                        "               c.client_name," +
                                        "               s.dayTime," +
                                        "               s.session_id," +
                                        "               s.duration," +
                                        "               s.notes" +
                                        "        From Session_Log s" +
                                        "        Inner Join Clients c On s.client_id=c.client_id" +
                                        "        Where s.client_id = $id;"

            fun getMultiSessionsExercises(sessionIDs: String) = "Select se.session_id as session_id," +
                                                        "               dat.exercise_id as exercise_id," +
                                                        "               dat.exercise_name as exercise_name," +
                                                        "               dat.exercise_type as exercise_type," +
                                                        "               dat.primary_mover_id as primary_mover_id," +
                                                        "               dat.primary_mover_name as primary_mover_name," +
                                                        "               dat.secondary_movers_ids as secondary_movers_ids," +
                                                        "               dat.secondary_movers_names as secondary_movers_names," +
                                                        "               se.sets as sets," +
                                                        "               se.reps as reps," +
                                                        "               se.resistance as resistance," +
                                                        "               se.exercise_order as exercise_order" +
                                                        "        From Session_Exercises se" +
                                                        "        Inner Join (" +
                                                        "            Select e.exercise_id," +
                                                        "                   e.exercise_name," +
                                                        "                   e.exercise_type," +
                                                        "                   e.primary_mover_id," +
                                                        "                   (" +
                                                        "                        Case" +
                                                        "                            When e.exercise_type = 1 then m.muscle_name" +
                                                        "                            When e.exercise_type = 2 or e.exercise_type = 3 then j.joint_name" +
                                                        "                        End" +
                                                        "                    ) as 'primary_mover_name'," +
                                                        "                   ifnull" +
                                                        "                    (" +
                                                        "                        (" +
                                                        "                            Select group_concat(s.secondary_mover_id)" +
                                                        "                            From secondary_movers s" +
                                                        "                            Where s.exercise_id = e.exercise_id" +
                                                        "                            Group By s.exercise_id" +
                                                        "                        )," +
                                                        "                        '0'" +
                                                        "                    ) as 'secondary_movers_ids'," +
                                                        "                   ifnull" +
                                                        "                    (" +
                                                        "                        (" +
                                                        "                            Select group_concat(" +
                                                        "                                (" +
                                                        "                                    Case" +
                                                        "                                        When e.exercise_type = 1 then m.muscle_name" +
                                                        "                                        When e.exercise_type = 2 or e.exercise_type = 3 then j.joint_name" +
                                                        "                                    End" +
                                                        "                                )" +
                                                        "                            )" +
                                                        "                            From secondary_movers s" +
                                                        "                            left join Muscles m on s.secondary_mover_id=m.muscle_id" +
                                                        "                            left join Joints j on s.secondary_mover_id=j.joint_id" +
                                                        "                            Where s.exercise_id = e.exercise_id" +
                                                        "                            Group By s.exercise_id" +
                                                        "                        )," +
                                                        "                        '0'" +
                                                        "                    ) as 'secondary_movers_names'" +
                                                        "            From Exercises e" +
                                                        "            left join Muscles m on e.primary_mover_id=m.muscle_id" +
                                                        "            left join Joints j on e.primary_mover_id=j.joint_id" +
                                                        "        ) dat on dat.exercise_id=se.exercise_id" +
                                                        "        Where se.session_id in ($sessionIDs);"

            fun getSession(id: Int) =   "Select s.client_id," +
                                "               c.client_name," +
                                "               s.dayTime," +
                                "               s.session_id," +
                                "               s.duration," +
                                "               s.notes" +
                                "        From Session_Log s" +
                                "        Inner Join Clients c On s.client_id=c.client_id" +
                                "        Where s.session_id = $id;"

            fun getSessionSessionChanges(id: Int, dayTime: String) =    "Select duration" +
                                                                "        From Session_Changes" +
                                                                "        Where client_id = $id" +
                                                                "        And datetime(change_dayTime) = datetime('$dayTime');"

            fun getSessionExercises(id: Int) =  "Select dat.exercise_id as exercise_id," +
                                        "               dat.exercise_name as exercise_name," +
                                        "               dat.exercise_type as exercise_type," +
                                        "               dat.primary_mover_id as primary_mover_id," +
                                        "               dat.primary_mover_name as primary_mover_name," +
                                        "               dat.secondary_movers_ids as secondary_movers_ids," +
                                        "               dat.secondary_movers_names as secondary_movers_names," +
                                        "               se.sets as sets," +
                                        "               se.reps as reps," +
                                        "               se.resistance as resistance," +
                                        "               se.exercise_order as exercise_order" +
                                        "        From Session_Exercises se" +
                                        "        Inner Join (" +
                                        "            Select e.exercise_id," +
                                        "                   e.exercise_name," +
                                        "                   e.exercise_type," +
                                        "                   e.primary_mover_id," +
                                        "                   (" +
                                        "                        Case" +
                                        "                            When e.exercise_type = 1 then m.muscle_name" +
                                        "                            When e.exercise_type = 2 or e.exercise_type = 3 then j.joint_name" +
                                        "                        End" +
                                        "                    ) as 'primary_mover_name'," +
                                        "                   ifnull" +
                                        "                    (" +
                                        "                        (" +
                                        "                            Select group_concat(s.secondary_mover_id)" +
                                        "                            From secondary_movers s" +
                                        "                            Where s.exercise_id = e.exercise_id" +
                                        "                            Group By s.exercise_id" +
                                        "                        )," +
                                        "                        '0'" +
                                        "                    ) as 'secondary_movers_ids'," +
                                        "                   ifnull" +
                                        "                    (" +
                                        "                        (" +
                                        "                            Select group_concat(" +
                                        "                                (" +
                                        "                                    Case" +
                                        "                                        When e.exercise_type = 1 then m.muscle_name" +
                                        "                                        When e.exercise_type = 2 or e.exercise_type = 3 then j.joint_name" +
                                        "                                    End" +
                                        "                                )" +
                                        "                            )" +
                                        "                            From secondary_movers s" +
                                        "                            left join Muscles m on s.secondary_mover_id=m.muscle_id" +
                                        "                            left join Joints j on s.secondary_mover_id=j.joint_id" +
                                        "                            Where s.exercise_id = e.exercise_id" +
                                        "                            Group By s.exercise_id" +
                                        "                        )," +
                                        "                        '0'" +
                                        "                    ) as 'secondary_movers_names'" +
                                        "            From Exercises e" +
                                        "            left join Muscles m on e.primary_mover_id=m.muscle_id" +
                                        "            left join Joints j on e.primary_mover_id=j.joint_id" +
                                        "        ) dat on dat.exercise_id=se.exercise_id" +
                                        "        Where se.session_id = $id;"

            fun checkSessionLog(id: Int) =  "Select client_id" +
                                    "        From Session_Log s" +
                                    "        Where s.session_id = $id;"

            fun getAddSessionClients(currentClients: String) =  "Select *" +
                                                        "        From Clients c" +
                                                        "        Where c.client_id Not in ($currentClients)" +
                                                        "        And (c.banked_sessions > 0" +
                                                        "             Or c.schedule_type = 0" +
                                                        "             );"

            fun getScheduleByDaySessionLog(date: String) =  "Select s.client_id," +
                                                    "               c.client_name," +
                                                    "               s.dayTime," +
                                                    "               s.session_id," +
                                                    "               s.duration," +
                                                    "               s.notes" +
                                                    "        From Session_Log s" +
                                                    "        Inner Join Clients c On s.client_id=c.client_id" +
                                                    "        Where date(dayTime) = date('$date');"

            fun getScheduleByDayClients(date: String, dayOfWeek: String) =  "Select c.client_id," +
                                                                    "               c.client_name," +
                                                                    "               c.$dayOfWeek," +
                                                                    "               c.${dayOfWeek}_duration" +
                                                                    "        From Clients c" +
                                                                    "        Where $dayOfWeek > 0" +
                                                                    "        And date(c.end_date) >= date('$date')" +
                                                                    "        And date(c.start_date) <= date('$date')" +
                                                                    "        And c.schedule_type        = 1" +
                                                                    "        And Not" +
                                                                    "        (c.client_id in (Select client_id" +
                                                                    "                         From Session_Changes" +
                                                                    "                         Where date(normal_dayTime) = date('$date')" +
                                                                    "                         )" +
                                                                    "         Or c.client_id in (Select s.client_id" +
                                                                    "                            From Session_Log s" +
                                                                    "                            Where date(dayTime) = date('$date')" +
                                                                    "                            )" +
                                                                    "         );"

            fun getScheduleByDaySessionChanges(date: String) =  "Select sc.client_id," +
                                                        "               c.client_name," +
                                                        "               sc.change_dayTime," +
                                                        "               sc.duration" +
                                                        "        From Session_Changes sc" +
                                                        "        Inner Join Clients c on sc.client_id=c.client_id" +
                                                        "        Where date(sc.change_dayTime) = date('$date')" +
                                                        "        And Not sc.client_id in" +
                                                        "        (Select s.client_id" +
                                                        "         From Session_Log s" +
                                                        "         Where date(dayTime) = date('$date')" +
                                                        "        );"

            fun insertChange(id: Int, normalDayTime: String, changeDayTime: String, duration: Int)= "Insert Into Session_Changes(client_id," +
                                                                                            "                                    normal_dayTime," +
                                                                                            "                                    change_dayTime," +
                                                                                            "                                    duration" +
                                                                                            "                                    )" +
                                                                                            "                             Values($id," +
                                                                                            "                                    '$normalDayTime'," +
                                                                                            "                                    '$changeDayTime'," +
                                                                                            "                                    $duration);"

            fun checkChange(id: Int, changeDayTime: String) =   "Select client_id" +
                                                        "        From Session_Changes" +
                                                        "        Where client_id = $id" +
                                                        "        And datetime(change_dayTime) = datetime('$changeDayTime');"

            fun updateChange(id: Int, oldDayTime: String, newDayTime: String, duration: Int) =  "Update Session_Changes" +
                                                                                        "        Set change_dayTime = '$newDayTime'," +
                                                                                        "            duration = $duration" +
                                                                                        "        Where client_id = $id" +
                                                                                        "        And datetime(change_dayTime) = datetime('$oldDayTime');"

            fun deleteChange(id: Int, changeDayTime: String) =  "Delete From Session_Changes" +
                                                        "        Where client_id = $id" +
                                                        "        And datetime(change_dayTime) = datetime('$changeDayTime');"

            fun getLastOccurrence(clientID: Int, exerciseID: Int) = "Select sets," +
                                                            "               reps," +
                                                            "               resistance," +
                                                            "               exercise_order" +
                                                            "        From Session_Exercises se" +
                                                            "        Inner Join Session_Log s On se.session_id=s.session_id" +
                                                            "        Where client_id = $clientID" +
                                                            "        And exercise_id = $exerciseID" +
                                                            "        Order By dayTime Desc" +
                                                            "        Limit 1;"

            fun getAllOccurrences(clientID: Int, exerciseID: Int) = "Select sets," +
                                                            "               reps," +
                                                            "               resistance," +
                                                            "               exercise_order" +
                                                            "        From Session_Exercises pe" +
                                                            "        Inner Join Session_Log s On se.session_id=s.session_id" +
                                                            "        Where client_id = $clientID" +
                                                            "        And exercise_id = $exerciseID" +
                                                            "        Order By dayTime Desc;"

            fun getUserSettings() = "Select default_duration," +
                            "               clock24" +
                            "        From User_Settings;"

            fun setUserSettings(duration: Int, clock24: Int) =  "Update User_Settings" +
                                                        "        Set default_duration = $duration," +
                                                        "            clock24 = $clock24;"

            fun cleanSessionChanges(currentDay: String) =   "Delete From Session_Changes" +
                                                    "        Where date(change_dayTime) < date('$currentDay')" +
                                                    "        And date(normal_dayTime) < date('$currentDay')" +
                                                    "        And date((Select last_clean" +
                                                    "                  From User_Settings" +
                                                    "                  )) < date('$currentDay');"

            const val getAllPrograms = "Select * " +
                                    "   From Programs;"

            fun getProgram(id: Int) = " Select * " +
                                    "   From Programs" +
                                    "   Where program_id = $id;"

            fun getMultiProgramsExercises(programIDs: String) = "Select pe.program_id as program_id," +
                                                        "               dat.exercise_id as exercise_id," +
                                                        "               dat.exercise_name as exercise_name," +
                                                        "               dat.exercise_type as exercise_type," +
                                                        "               dat.primary_mover_id as primary_mover_id," +
                                                        "               dat.primary_mover_name as primary_mover_name," +
                                                        "               dat.secondary_movers_ids as secondary_movers_ids," +
                                                        "               dat.secondary_movers_names as secondary_movers_names," +
                                                        "               pe.sets as sets," +
                                                        "               pe.reps as reps," +
                                                        "               pe.day as day," +
                                                        "               pe.exercise_order as exercise_order" +
                                                        "        From Program_Exercises pe" +
                                                        "        Inner Join (" +
                                                        "            Select e.exercise_id," +
                                                        "                   e.exercise_name," +
                                                        "                   e.exercise_type," +
                                                        "                   e.primary_mover_id," +
                                                        "                   (" +
                                                        "                        Case" +
                                                        "                            When e.exercise_type = 1 then m.muscle_name" +
                                                        "                            When e.exercise_type = 2 or e.exercise_type = 3 then j.joint_name" +
                                                        "                        End" +
                                                        "                    ) as 'primary_mover_name'," +
                                                        "                   ifnull" +
                                                        "                    (" +
                                                        "                        (" +
                                                        "                            Select group_concat(s.secondary_mover_id)" +
                                                        "                            From secondary_movers s" +
                                                        "                            Where s.exercise_id = e.exercise_id" +
                                                        "                            Group By s.exercise_id" +
                                                        "                        )," +
                                                        "                        '0'" +
                                                        "                    ) as 'secondary_movers_ids'," +
                                                        "                   ifnull" +
                                                        "                    (" +
                                                        "                        (" +
                                                        "                            Select group_concat(" +
                                                        "                                (" +
                                                        "                                    Case" +
                                                        "                                        When e.exercise_type = 1 then m.muscle_name" +
                                                        "                                        When e.exercise_type = 2 or e.exercise_type = 3 then j.joint_name" +
                                                        "                                    End" +
                                                        "                                )" +
                                                        "                            )" +
                                                        "                            From secondary_movers s" +
                                                        "                            left join Muscles m on s.secondary_mover_id=m.muscle_id" +
                                                        "                            left join Joints j on s.secondary_mover_id=j.joint_id" +
                                                        "                            Where s.exercise_id = e.exercise_id" +
                                                        "                            Group By s.exercise_id" +
                                                        "                        )," +
                                                        "                        '0'" +
                                                        "                    ) as 'secondary_movers_names'" +
                                                        "            From Exercises e" +
                                                        "            left join Muscles m on e.primary_mover_id=m.muscle_id" +
                                                        "            left join Joints j on e.primary_mover_id=j.joint_id" +
                                                        "        ) dat on dat.exercise_id=pe.exercise_id" +
                                                        "        Where pe.program_id in ($programIDs);"
        }
    }
}