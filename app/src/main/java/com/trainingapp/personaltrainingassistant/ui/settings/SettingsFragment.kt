package com.trainingapp.personaltrainingassistant.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.trainingapp.personaltrainingassistant.database.DatabaseOperations
import com.trainingapp.personaltrainingassistant.R
import kotlinx.android.synthetic.main.fragment_settings.*
import java.lang.ClassCastException

class SettingsFragment: Fragment(), View.OnClickListener {

    private lateinit var databaseOperations: DatabaseOperations
    private var userSettings = ArrayList<Int>()
    private lateinit var iFragmentToActivity: IFragmentToActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            iFragmentToActivity = context as IFragmentToActivity
        } catch (e: ClassCastException){
            e.printStackTrace()
            Toast.makeText(context, "Could not cast context as IFragmentToActivity", Toast.LENGTH_LONG).show()
        }
        databaseOperations =
            DatabaseOperations(
                context
            )
        userSettings = databaseOperations.getUserSettings()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (userSettings[1] == 1)
            swIs24Hour.isChecked = true
        if (userSettings[0] > 0)
            etxtDefaultSessionDuration.setText(userSettings[0].toString())
        btnConfirmUserSettings.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (userSettings[0] != etxtDefaultSessionDuration.text.toString().toInt())
            userSettings[0] = etxtDefaultSessionDuration.text.toString().toInt()
        if (swIs24Hour.isChecked)
            userSettings[1] = 1
        else
            userSettings[1] = 0
        Snackbar.make(view, "Default Duration: ${userSettings[0]}, 24Hour: ${userSettings[1]==1}", Snackbar.LENGTH_LONG).setAction("Action", null).show()
        databaseOperations.setUserSettings(userSettings)
        iFragmentToActivity.communicateToActivity()
    }

    interface IFragmentToActivity {
        fun communicateToActivity()
    }
}