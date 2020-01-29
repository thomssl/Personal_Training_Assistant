package com.trainingapp.trainingassistant.objects

class Program (var id: Int, var name: String, var exercises: ArrayList<ExerciseSession>) {

    //used to validate that a program has exercises
    // stops the user from removing all exercises from a session than updating or confirming a session that has never had any exercises
    fun hasExercises(): Boolean = exercises.size > 0

    fun addExercise(exerciseSession: ExerciseSession){
        exercises.add(exerciseSession)
        exercises.sort()
    }

    fun updateExercise(exerciseSession: ExerciseSession, position: Int){
        exercises[position] = exerciseSession
        exercises.sort()
    }

    fun removeExercise(exerciseSession: ExerciseSession){
        exercises.remove(exerciseSession)
        exercises.sort()
    }

    /**
     * Method to remove an ExerciseSession based upon a passed Exercise object
     * Finds index of the ExerciseSession that contains the Exercise object passed
     * if the index is valid (index > -1), remove the ExerciseSession at that index and sort the remaining sessions
     * if the index is invalid, do nothing
     */
    fun removeExercise(exercise: Exercise){
        var index = -1
        for (i in exercises.indices){
            if (exercise.id == exercises[i].id){
                index = i
                break
            }
        }
        if (index >= 0) {
            exercises.removeAt(index)
            exercises.sort()
        }
    }

    /**
     * Method to get an ExerciseSession based upon a passed Exercise object
     * Finds the index of the ExerciseSession that contains the Exercise object passed
     * if the index is valid (index > -1), return the ExerciseSession at that index
     * if the index is invalid, return an empty ExerciseSession
     */
    fun getExercise(exercise: Exercise): ExerciseSession {
        var index = -1
        for (i in exercises.indices){
            if (exercise.id == exercises[i].id){
                index = i
                break
            }
        }
        return if (index > -1) getExercise(index) else ExerciseSession(exercise, "", "", "", 0)
    }

    fun getExercise(index: Int): ExerciseSession = exercises[index]
    fun getExerciseCount(): Int = exercises.size

    private fun getAllExerciseInserts(clientID: Int, dayTime: String): String{
        val builder = StringBuilder()
        exercises.forEach { builder.append("(${if (id == 0) "(Select program_id From Programs Where client_id = $clientID And datetime(dayTime) = datetime('$dayTime'))" else id.toString()}, ${it.id}, '${it.sets}', '${it.reps}', '${it.resistance}'," +
                " ${it.order}),") }
        if (builder.isNotBlank())
            builder.deleteCharAt(builder.lastIndex)
        return builder.toString()
    }

    fun getInsertProgramsCommand(clientID: Int, dayTime: String): String = "Insert Into Programs(client_id, dayTime, program_name) Values($clientID, '$dayTime', '$name');"
    fun getInsertProgramExercisesCommand(clientID: Int, dayTime: String): String =  if (hasExercises()) "Insert Into Program_Exercises(program_id, exercise_id, sets, reps, resistance, exercise_order) Values ${getAllExerciseInserts(clientID, 
        dayTime)}" else ""
    fun getUpdateProgramsCommand(dayTime: String, oldDayTime: String): String = if (oldDayTime.isNotBlank()) "Update Programs set dayTime = '$dayTime'" else ""
    fun getUpdateProgramExercisesCommand(): String = if (hasExercises()) "Delete From ProgramExercises Where program_id = $id; ${getInsertProgramExercisesCommand(-1, "")}" else "Delete From ProgramExercises Where program_id = $id;"
    fun getDeleteProgramCommand(clientID: Int, dayTime: String): String = "Delete From Program_Exercises Where program_id = ${id}; Delete From Programs Where client_id = $clientID And dateTime(dayTime) = dateTime('$dayTime')"
}