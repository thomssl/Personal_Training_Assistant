package com.trainingapp.personaltrainingassistant.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.trainingapp.personaltrainingassistant.R
import com.trainingapp.personaltrainingassistant.objects.MuscleJoint
import kotlinx.android.synthetic.main.add_edit_muscle_dialog.*
import java.lang.IllegalStateException

class AddEditMuscleDialog(private val muscleJoint: MuscleJoint, private val confirmListener: (MuscleJoint) -> Boolean): DialogFragment() {

    private lateinit var txtName: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val view = View.inflate(context, R.layout.add_edit_muscle_dialog, null)
            txtName = view.findViewById(R.id.etxtAddEditMuscleName)
            txtName.setText(muscleJoint.name)
            val builder = AlertDialog.Builder(it)
            builder.setView(view)
                .setPositiveButton(R.string.confirm) {_, _ -> }
                .setNegativeButton(R.string.cancel) {_, _ -> dismiss() }
                .setTitle(if (muscleJoint.id == 0) R.string.add_muscle_title else R.string.edit_muscle_title)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog).getButton(Dialog.BUTTON_POSITIVE).setOnClickListener { if (confirmListener(MuscleJoint(muscleJoint.id, txtName.text.toString()))) dismiss() }
    }
}