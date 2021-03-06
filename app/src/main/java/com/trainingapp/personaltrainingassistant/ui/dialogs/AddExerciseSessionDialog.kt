package com.trainingapp.personaltrainingassistant.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.trainingapp.personaltrainingassistant.database.DatabaseOperations
import com.trainingapp.personaltrainingassistant.objects.Exercise
import com.trainingapp.personaltrainingassistant.R
import com.trainingapp.personaltrainingassistant.ui.adapters.SearchForExerciseSession
import java.lang.IllegalStateException

class AddExerciseSessionDialog(private val clientID: Int, private val  confirmListener: (AddExerciseSessionDialog) -> Boolean): DialogFragment() {

    private lateinit var databaseOperations: DatabaseOperations
    var exercises = ArrayList<Exercise>()
    var exerciseNames = ArrayList<String>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val view = View.inflate(context, R.layout.add_exercise_dialog, null)
            val txtNames = view.findViewById<AutoCompleteTextView>(R.id.actxtAddExerciseName)
            txtNames.setAdapter(SearchForExerciseSession(context!!, R.layout.simple_autocomplete_item, exercises))
            txtNames.setOnItemClickListener{ _, _, _, _ ->
                val exerciseSession = databaseOperations.getLastOccurrence(exercises[exerciseNames.indexOf(txtNames.text.toString())], clientID)
                if (exerciseSession.hasData()) {
                    view.findViewById<EditText>(R.id.etxtAddResistance).setText(exerciseSession.resistance)
                    view.findViewById<EditText>(R.id.etxtAddReps).setText(exerciseSession.reps)
                    view.findViewById<EditText>(R.id.etxtAddSets).setText(exerciseSession.sets)
                    view.findViewById<EditText>(R.id.etxtAddExerciseOrder).setText(exerciseSession.order.toString())
                }
            }
            val builder = AlertDialog.Builder(it)
            builder.setView(view)
                .setPositiveButton(R.string.confirm) { _, _ -> }
                .setNegativeButton(R.string.cancel){ _, _ -> dismiss()}
                .setTitle(R.string.titleAddExerciseDialog)
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
        if (resources.configuration.smallestScreenWidthDp > 600)
         dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        (dialog as AlertDialog).getButton(Dialog.BUTTON_POSITIVE).setOnClickListener { if (confirmListener(this)) dismiss() }
    }
}