package com.trainingapp.personaltrainingassistant.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.trainingapp.personaltrainingassistant.R
import java.lang.IllegalStateException

class ChangeDurationDialog(private val duration: Int, private val confirmListener: (String) -> Boolean): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val view = View.inflate(context, R.layout.change_duration_dialog, null)
            val txtDuration = view.findViewById<EditText>(R.id.etxtSessionChangeDuration)
            txtDuration.setText(if (duration != 0) duration.toString() else "")
            val builder = AlertDialog.Builder(it)
            builder.setView(view)
                .setPositiveButton(R.string.confirm) {_,_ -> if (confirmListener(txtDuration.text.toString())) dismiss()}
                .setNegativeButton(R.string.cancel) {_,_ -> dismiss()}
                .setTitle(R.string.btnChangeSessionDuration)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}