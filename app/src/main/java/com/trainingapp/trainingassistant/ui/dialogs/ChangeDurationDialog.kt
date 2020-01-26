package com.trainingapp.trainingassistant.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.trainingapp.trainingassistant.R
import java.lang.IllegalStateException

class ChangeDurationDialog(private val duration: Int, private val confirmListener: (String) -> Boolean): DialogFragment() {

    private lateinit var txtDuration: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val view = View.inflate(context, R.layout.change_duration_dialog, null)
            txtDuration = view.findViewById(R.id.etxtSessionChangeDuration)
            txtDuration.setText(if (duration != 0) duration.toString() else "")
            val builder = AlertDialog.Builder(it)
            builder.setView(view)
                .setPositiveButton(R.string.confirm) {_,_ -> }
                .setNegativeButton(R.string.cancel) {_,_ -> dismiss()}
                .setTitle(R.string.btnChangeSessionDuration)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog).getButton(Dialog.BUTTON_POSITIVE).setOnClickListener { if (confirmListener(txtDuration.text.toString())) dismiss() }
    }
}