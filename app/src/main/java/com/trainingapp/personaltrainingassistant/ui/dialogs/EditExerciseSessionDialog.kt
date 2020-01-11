package com.trainingapp.personaltrainingassistant.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.trainingapp.personaltrainingassistant.database.DatabaseOperations
import com.trainingapp.personaltrainingassistant.objects.Exercise
import com.trainingapp.personaltrainingassistant.objects.ExerciseSession
import com.trainingapp.personaltrainingassistant.R
import java.lang.IllegalStateException

class EditExerciseSessionDialog(val exerciseSession: ExerciseSession, private val position: Int, private val  confirmListener: (EditExerciseSessionDialog, Int) -> Boolean): DialogFragment() {

    private lateinit var databaseOperations: DatabaseOperations
    var exercises = ArrayList<Exercise>()
    private var exerciseNames = ArrayList<String>()

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
        exercises.forEach{exerciseNames.add(it.name)}
    }

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog).getButton(Dialog.BUTTON_POSITIVE).setOnClickListener { if (confirmListener(this, position)) dismiss() }
    }
}