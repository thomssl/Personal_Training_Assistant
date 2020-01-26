package com.trainingapp.trainingassistant.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.database.DatabaseOperations2
import kotlinx.android.synthetic.main.fragment_settings.*

/**
 * Fragment to display user settings and allow the user to make changes to the settings
 */
class SettingsFragment: Fragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private lateinit var databaseOperations: DatabaseOperations2
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
        databaseOperations = DatabaseOperations2(context)
        userSettings = databaseOperations.getUserSettings()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (userSettings[1] == 1)//userSettings[1] is 0 or 1 (true or false) for 24 hour clock
            swIs24Hour.isChecked = true
        if (userSettings[0] > 0)//userSettings[0] is the default duration (1-120)
            etxtDefaultSessionDuration.setText(userSettings[0].toString())
        btnConfirmUserSettings.setOnClickListener(this)
        swIs24Hour.setOnCheckedChangeListener(this)
    }

    /**
     * Method to handle btnConfirmUserSettings onClick event
     * Validates duration. If valid duration, sets user settings in the database, prompts user and then returns the user to 'Schedule' drawer action
     * If not valid or SQL error occurs, prompt user of error
     */
    override fun onClick(view: View) {
        val strDuration = etxtDefaultSessionDuration.text.toString()
        if (strDuration.isDigitsOnly() && strDuration.isNotBlank()) {
            try {
                val duration = strDuration.toInt()
                if (duration in 1..120) {
                    userSettings[0] = duration
                    Snackbar.make(view, "Default Duration: ${userSettings[0]}, 24Hour: ${userSettings[1] == 1}", Snackbar.LENGTH_LONG).setAction("Action", null).show()
                    if (databaseOperations.setUserSettings(userSettings))
                        iFragmentToActivity.returnToSchedule()
                    else
                        Snackbar.make(view, "Error. User settings update failed", Snackbar.LENGTH_LONG).show()
                } else
                    Snackbar.make(view, "Error. Duration is not valid. See Wiki 'Input Fields'", Snackbar.LENGTH_LONG).show()
            } catch (ex: NumberFormatException){
                ex.printStackTrace()
                Snackbar.make(view, "Error. Duration is not an integer", Snackbar.LENGTH_LONG).show()
            }
        } else
            Snackbar.make(view, "Error. Duration is not an integer", Snackbar.LENGTH_LONG).show()
    }

    override fun onCheckedChanged(view: CompoundButton?, checked: Boolean) {
        if (checked) userSettings[1] = 1 else userSettings[1] = 0
    }

    interface IFragmentToActivity {
        fun returnToSchedule()
    }
}