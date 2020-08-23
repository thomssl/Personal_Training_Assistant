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
import com.trainingapp.trainingassistant.objects.ExerciseSession

class EditExerciseSessionDialog(
    val exerciseSession: ExerciseSession,
    private val position: Int,
    private val confirmListener: (EditExerciseSessionDialog, Int) -> Boolean
): DialogFragment() {

    private lateinit var databaseOperations: DatabaseOperations
    lateinit var exercises: List<Exercise>
    private lateinit var exerciseNames: List<String>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val view = View.inflate(context, R.layout.edit_exercise_dialog, null)
            val txtName = view.findViewById<TextView>(R.id.lblEditExerciseName)
            txtName.text = exerciseSession.name
            view.findViewById<EditText>(R.id.etxtEditResistance).setText(exerciseSession.resistance)
            view.findViewById<EditText>(R.id.etxtEditSets).setText(exerciseSession.sets)
            view.findViewById<EditText>(R.id.etxtEditReps).setText(exerciseSession.reps)
            view.findViewById<EditText>(R.id.etxtEditExerciseOrder).setText(exerciseSession.order.toString())
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