package com.trainingapp.trainingassistant.objects

class Program (
    var id: Int,
    var name: String,
    var days: Int,
    var desc: String,
    var exercises: ArrayList<ExerciseProgram>
) {

    //used to validate that a program has exercises
    // stops the user from removing all exercises from a session than updating or confirming a session that has never had any exercises
    private fun hasExercises(): Boolean = exercises.size > 0

    fun addExercise(ExerciseProgram: ExerciseProgram){
        exercises.add(ExerciseProgram)
        exercises.sort()
    }

    fun updateExercise(ExerciseProgram: ExerciseProgram, position: Int){
        exercises[position] = ExerciseProgram
        exercises.sort()
    }

    fun removeExercise(ExerciseProgram: ExerciseProgram){
        exercises.remove(ExerciseProgram)
        exercises.sort()
    }

    /**
     * Method to remove an ExerciseProgram based upon a passed Exercise object
     * Finds index of the ExerciseProgram that contains the Exercise object passed
     * if the index is valid (index > -1), remove the ExerciseProgram at that index and sort the remaining sessions
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
     * Method to get an ExerciseProgram based upon a passed Exercise object
     * Finds the index of the ExerciseProgram that contains the Exercise object passed
     * if the index is valid (index > -1), return the ExerciseProgram at that index
     * if the index is invalid, return an empty ExerciseProgram
     */
    fun getExercise(exercise: Exercise): ExerciseProgram {
        var index = -1
        for (i in exercises.indices){
            if (exercise.id == exercises[i].id){
                index = i
                break
            }
        }
        return if (index > -1) getExercise(index) else ExerciseProgram(exercise, "", "", 0, 0)
    }

    fun getExercise(index: Int): ExerciseProgram = exercises[index]
    fun getExerciseCount(): Int = exercises.size

    private fun getAllExerciseInserts(): String{
        val strProgramID = if (id == 0)
            "(Select program_id From Programs Where program_name = $name)"
        else
            id.toString()
        return exercises.joinToString { "($strProgramID, ${it.id}, '${it.sets}', '${it.reps}', '${it.day}', ${it.order})" }
    }
    private fun getDeleteProgramCommand() = "Delete From Programs Where Where program_id = $id;"
    private fun getDeleteProgramExercisesCommand() = "Delete From Program_Exercises Where program_id = $id;"
    private fun getInsertProgramCommand() = "Insert Into Programs(program_name, program_desc, program_days) Values('$name', '$desc', $days);"
    private fun getInsertProgramExercisesCommand(): String {
        return if (hasExercises())
            "Insert Into Program_Exercises(program_id, exercise_id, sets, reps, day, exercise_order) Values ${getAllExerciseInserts()}"
        else ""
    }
    fun getInsertProgramCommands() : List<String>{
        return listOf(
            getInsertProgramCommand(),
            getInsertProgramExercisesCommand()
        )
    }
    fun getUpdateProgramCommands(): List<String> {
        return listOf(
            "Update Programs " +
            "Set program_name = '$name', program_desc = '$desc', program_days = $days " +
            "Where program_id = $id;",
            getDeleteProgramExercisesCommand(),
            getInsertProgramExercisesCommand()
            )
    }
    fun getDeleteProgramCommands(): List<String> {
        return listOf(
            getDeleteProgramCommand(),
            getDeleteProgramExercisesCommand()
        )
    }
}