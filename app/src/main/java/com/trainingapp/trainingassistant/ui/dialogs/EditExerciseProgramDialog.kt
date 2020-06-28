package com.trainingapp.trainingassistant.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.database.DatabaseOperations
import com.trainingapp.trainingassistant.objects.Exercise
import com.trainingapp.trainingassistant.objects.ExerciseProgram

class EditExerciseProgramDialog(
    private val exerciseProgram: ExerciseProgram,
    private val position: Int,
    private val  confirmListener: (EditExerciseProgramDialog, Int) -> Boolean
): DialogFragment() {

    private lateinit var databaseOperations: DatabaseOperations
    lateinit var exercises: List<Exercise>
    private lateinit var exerciseNames: List<String>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val view = View.inflate(context, R.layout.edit_exercise_program_dialog, null)
            val txtName = view.findViewById<TextView>(R.id.lblEditProgramExerciseName)
            txtName.text = exerciseProgram.name
            view.findViewById<EditText>(R.id.etxtEditProgramExerciseDay).setText(exerciseProgram.day.toString())
            view.findViewById<EditText>(R.id.etxtEditProgramExerciseSets).setText(exerciseProgram.sets)
            view.findViewById<EditText>(R.id.etxtEditProgramExerciseReps).setText(exerciseProgram.reps)
            view.findViewById<EditText>(R.id.etxtEditProgramExerciseOrder).setText(exerciseProgram.order.toString())
            val builder = AlertDialog.Builder(it)
            builder.setView(view)
                .setPositiveButton(R.string.confirm) { _, _ -> }
                .setNegativeButton(R.string.cancel){ _, _ -> dismiss()}
                .setTitle(R.string.titleEditExerciseDialog)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        databaseOperations = DatabaseOperations(context)
        exercises = databaseOperations.getAllExercises()
        exerciseNames = exercises.map { it.name }
    }

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog).getButton(Dialog.BUTTON_POSITIVE).setOnClickListener { if (confirmListener(this, position)) dismiss() }
    }
}