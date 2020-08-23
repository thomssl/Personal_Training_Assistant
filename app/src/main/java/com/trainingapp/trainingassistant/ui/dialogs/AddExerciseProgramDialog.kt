package com.trainingapp.trainingassistant.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AutoCompleteTextView
import androidx.fragment.app.DialogFragment
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.database.DatabaseOperations
import com.trainingapp.trainingassistant.objects.Exercise
import com.trainingapp.trainingassistant.ui.adapters.SearchForExerciseAutoComplete

class AddExerciseProgramDialog(private val  confirmListener: (AddExerciseProgramDialog) -> Boolean): DialogFragment() {

    private lateinit var databaseOperations: DatabaseOperations
    lateinit var exercises: List<Exercise>
    private lateinit var exerciseNames: List<String>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val view = View.inflate(context, R.layout.add_exercise_program_dialog, null)
            val txtNames = view.findViewById<AutoCompleteTextView>(R.id.actxtAddProgramExerciseName)
            txtNames.setAdapter(SearchForExerciseAutoComplete(context!!, R.layout.simple_autocomplete_item, exercises))
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
        exerciseNames = exercises.map { it.name }
    }

    override fun onStart() {
        super.onStart()
        if (resources.configuration.smallestScreenWidthDp > 600)
         dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        (dialog as AlertDialog).getButton(Dialog.BUTTON_POSITIVE).setOnClickListener { if (confirmListener(this)) dismiss() }
    }
}