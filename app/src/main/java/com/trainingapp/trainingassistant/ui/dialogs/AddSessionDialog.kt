package com.trainingapp.trainingassistant.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.DialogFragment
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.StaticFunctions
import com.trainingapp.trainingassistant.enumerators.ScheduleType
import com.trainingapp.trainingassistant.objects.Client
import com.trainingapp.trainingassistant.objects.Session
import java.util.*

class AddSessionDialog(
    private val clients: List<Client>,
    private val time: Date,
    private val confirmListener: (Session, ScheduleType) -> Boolean
): DialogFragment() {

    private lateinit var clientNames: List<String>
    private lateinit var btnTime: Button
    private lateinit var spnNames: Spinner
    private lateinit var txtDuration: EditText
    private lateinit var calendar: Calendar

    override fun onAttach(context: Context) {
        super.onAttach(context)
        calendar = Calendar.getInstance()
        calendar.time = time
        clientNames = clients.map { "${it.name}${if (it.scheduleType == ScheduleType.WEEKLY_CONSTANT) " (makeup session)" else ""}" }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val view = View.inflate(context, R.layout.add_session_dialog, null)
            btnTime = view.findViewById(R.id.btnAddSessionStartTime)
            spnNames = view.findViewById(R.id.spnAddSessionClients)
            txtDuration = view.findViewById(R.id.etxtAddSessionDuration)
            spnNames.adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_expandable_list_item_1, clientNames)
            btnTime.setOnClickListener {
                TimePickerDialog(
                    context,
                    R.style.DialogTheme,
                    {_: TimePicker, hour: Int, minute : Int ->
                        calendar[Calendar.HOUR_OF_DAY] = hour
                        calendar[Calendar.MINUTE] = minute
                        btnTime.text = StaticFunctions.getStrTimeAMPM(calendar.time)
                    },
                    0,
                    0,
                    false
                ).show()
            }
            val builder = AlertDialog.Builder(it)
                .setView(view)
                .setPositiveButton(R.string.confirm) {_,_ -> }
                .setNegativeButton(R.string.cancel) {_,_ -> dismiss()}
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog).getButton(Dialog.BUTTON_POSITIVE).setOnClickListener {
            val strDuration = txtDuration.text.toString()
            when {
                btnTime.text == getString(R.string.time_format) ->
                    Toast.makeText(context, "No Time Selected", Toast.LENGTH_LONG).show()
                !strDuration.isDigitsOnly() || strDuration.isBlank() ->
                    Toast.makeText(context, "Duration is blank or not an integer", Toast.LENGTH_LONG).show()
                strDuration.toInt() in 1..120 ->
                    Toast.makeText(context, "Duration not valid. See Wiki 'Input Fields'", Toast.LENGTH_LONG).show()
                else -> {
                    val client = clients[spnNames.selectedItemPosition]
                    val session = Session(
                        0,
                        client.id,
                        client.name,
                        StaticFunctions.getStrDateTime(calendar.time),
                        "",
                        strDuration.toInt(),
                        mutableListOf()
                    )
                    if (confirmListener(session, client.scheduleType))
                        dismiss()
                }
            }
        }
    }
}