package com.trainingapp.trainingassistant.objects

import android.database.Cursor
import com.trainingapp.trainingassistant.database.DBInfo

class Program (
    val id: Int,
    val name: String,
    val days: Int,
    val desc: String,
    val exercises: MutableList<ExerciseProgram>
) {

    companion object {
        val empty = Program(
            0,
            "",
            0,
            "",
            mutableListOf()
        )

        fun withCursor(it: Cursor): Program {
            return Program(
                it.getInt(it.getColumnIndex(DBInfo.ProgramsTable.ID)),
                it.getString(it.getColumnIndex(DBInfo.ProgramsTable.NAME)),
                it.getInt(it.getColumnIndex(DBInfo.ProgramsTable.DAYS)),
                it.getString(it.getColumnIndex(DBInfo.ProgramsTable.DESC)),
                mutableListOf()
            )
        }
    }

    //used to validate that a program has exercises
    // stops the user from removing all exercises from a session than updating or confirming a session that has never had any exercises
    private val hasExercises: Boolean
        get() = exercises.size > 0

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
    val exerciseCount: Int
        get() = exercises.size

    private val allExerciseInserts: String
        get() {
            val strProgramID = if (id == 0)
                "(Select program_id From Programs Where program_name = $name)"
            else
                id.toString()
            return exercises.joinToString { "($strProgramID, ${it.id}, '${it.sets}', '${it.reps}', '${it.day}', ${it.order})" }
        }
    private val deleteProgramCommand get() = "Delete From Programs Where Where program_id = $id;"
    private val deleteProgramExercisesCommand get() = "Delete From Program_Exercises Where program_id = $id;"
    private val insertProgramCommand get() = "Insert Into Programs(program_name, program_desc, program_days) Values('$name', '$desc', $days);"
    private val insertProgramExercisesCommand: String
        get() {
            return if (hasExercises)
                "Insert Into Program_Exercises(program_id, exercise_id, sets, reps, day, exercise_order) Values $allExerciseInserts"
            else ""
        }
    val insertProgramCommands: List<String>
        get() {
            return listOf(
                insertProgramCommand,
                insertProgramExercisesCommand
            )
        }
    val updateProgramCommands: List<String>
        get() {
            return listOf(
                "Update Programs " +
                        "Set program_name = '$name', program_desc = '$desc', program_days = $days " +
                        "Where program_id = $id;",
                deleteProgramExercisesCommand,
                insertProgramExercisesCommand
            )
        }
    val deleteProgramCommands: List<String>
        get() {
            return listOf(
                deleteProgramCommand,
                deleteProgramExercisesCommand
            )
        }
}