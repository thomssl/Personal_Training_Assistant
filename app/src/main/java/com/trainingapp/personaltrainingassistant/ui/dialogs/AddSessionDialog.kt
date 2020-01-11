package com.trainingapp.personaltrainingassistant.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.DialogFragment
import com.trainingapp.personaltrainingassistant.R
import com.trainingapp.personaltrainingassistant.StaticFunctions
import com.trainingapp.personaltrainingassistant.enumerators.ScheduleType
import com.trainingapp.personaltrainingassistant.objects.Client
import com.trainingapp.personaltrainingassistant.objects.Session
import java.util.*
import kotlin.collections.ArrayList

class AddSessionDialog(private val clients: ArrayList<Client>, private val calendar: Calendar, private val confirmListener: (Session, ScheduleType) -> Boolean): DialogFragment() {

    private val clientNames = ArrayList<String>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        clients.forEach { clientNames.add("${it.name}${if (it.scheduleType == ScheduleType.WEEKLY_CONSTANT) " (makeup session)" else ""}") }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val view = View.inflate(context, R.layout.add_session_dialog, null)
            val btnTime = view.findViewById<Button>(R.id.btnAddSessionStartTime)
            val txtNames = view.findViewById<Spinner>(R.id.spnAddSessionClients)
            val txtDuration = view.findViewById<EditText>(R.id.etxtAddSessionDuration)
            txtNames.adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_expandable_list_item_1, clientNames)
            btnTime.setOnClickListener { TimePickerDialog(context, R.style.DialogTheme, {_: TimePicker, hour: Int, minute : Int -> calendar[Calendar.HOUR_OF_DAY] = hour; calendar[Calendar.MINUTE] = minute; btnTime.text = StaticFunctions.getStrTimeAMPM(calendar)}, 0, 0, false).show() }
            val builder = AlertDialog.Builder(it)
                .setView(view)
                .setPositiveButton(R.string.confirm) {_,_ ->
                    val duration = txtDuration.text.toString()
                    if (btnTime.text != getString(R.string.time_format) && duration.isDigitsOnly() && duration.isNotBlank()) {
                        val client = clients[txtNames.selectedItemPosition]
                        if (confirmListener(Session(client.id, client.name, StaticFunctions.getStrDateTime(calendar), ArrayList(), "", duration.toInt()),client.scheduleType))
                            dismiss()
                    } else {
                        Toast.makeText(context, "No Time Selected", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton(R.string.cancel) {_,_ -> dismiss()}
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}