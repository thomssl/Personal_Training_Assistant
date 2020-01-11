package com.trainingapp.personaltrainingassistant.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.trainingapp.personaltrainingassistant.R
import com.trainingapp.personaltrainingassistant.database.DatabaseOperations
import com.trainingapp.personaltrainingassistant.enumerators.ScheduleType
import com.trainingapp.personaltrainingassistant.objects.MuscleJoint
import com.trainingapp.personaltrainingassistant.objects.Session
import com.trainingapp.personaltrainingassistant.ui.dialogs.AddEditMuscleDialog
import com.trainingapp.personaltrainingassistant.ui.dialogs.AddSessionDialog
import com.trainingapp.personaltrainingassistant.ui.schedule.ScheduleFragment
import com.trainingapp.personaltrainingassistant.ui.settings.SettingsFragment
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_schedule.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

/**
 * Activity that holds the NavigationDrawer. When the drawer items are selected the appropriate Fragment is loaded and displayed
 * A common FloatingActionButton is displayed over some Fragments and depending upon the open Fragment, an add process is started
 */
class MainActivity : AppCompatActivity(),
                        View.OnClickListener,
                        NavController.OnDestinationChangedListener,
                        SettingsFragment.IFragmentToActivity,
                        ScheduleFragment.IFragmentToActivity {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var databaseOperations: DatabaseOperations
    private val calendar: Calendar = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        loadPreBuiltDatabase()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        databaseOperations = DatabaseOperations(this)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener(this)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_schedule,
            R.id.nav_clients,
            R.id.nav_exercises,
            R.id.nav_muscles,
            R.id.nav_wiki,
            R.id.nav_settings,
            R.id.nav_camera
        ), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener(this)
    }

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }*/

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            //R.id.action_settings ->
            R.id.action_wiki -> {
                val intent = Intent(this, WikiActivity::class.java)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }*/

    override fun onSupportNavigateUp(): Boolean {
        navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * Method used to save a prebuilt database to the applications data folder. This includes base information and does nothing if the database file already exists in the data folder
     */
    private fun loadPreBuiltDatabase() {
        val file = File(getString(R.string.filePath))
        if (!file.exists()) {
            val assetManager = assets
            try {
                val input = assetManager.open("data.db")
                val out: OutputStream = FileOutputStream(getString(R.string.filePath))
                val buffer = ByteArray(1024)
                var read = input.read(buffer)
                while (read != -1) {
                    out.write(buffer, 0, read)
                    read = input.read(buffer)
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    /**
     * Method to handle the FloatingActionButton's onClick event. Proceeds with the appropriate add process depending upon the current NavigationController destination
     * @param view FloatingActionButton view used to create Snackbars
     */
    override fun onClick(view: View) {
        when (navController.currentDestination!!.id){
            R.id.nav_schedule -> {//uses updated date from CalendarView and creates an AddSessionDialog to insert the new session
                val dialog = AddSessionDialog(databaseOperations.getAddSessionsClientsByDay(calendar), calendar) { session, scheduleType -> addSessionConfirm(session,scheduleType) }
                dialog.show(supportFragmentManager, "Add Session")
            }
            R.id.nav_clients -> {//creates and uses Intent to start an AddEditClientActivity. Sends an invalid id to tell the activity it is a new Client
                val intent = Intent(this, AddEditClientActivity::class.java)
                intent.putExtra("id", 0)
                startActivity(intent)
            }
            R.id.nav_exercises -> {//creates and uses Intent to start an AddEditExerciseActivity. Sends an invalid id to tell the activity it is a new Exercise
                val intent = Intent(this, AddEditExerciseActivity::class.java)
                intent.putExtra("id", 0)
                startActivity(intent)
            }
            R.id.nav_camera -> {
                Snackbar.make(view,"Name: Camera", Snackbar.LENGTH_LONG).show()
            }
            R.id.nav_muscles -> {//creates an AddEditMuscleDialog and sends a blank MuscleJoint object to denote a new Muscle
                val dialog = AddEditMuscleDialog(MuscleJoint(0, "")) {muscleJoint -> addEditConfirm(muscleJoint) }
                dialog.show(supportFragmentManager, "Add Muscle")
            }
            else -> Snackbar.make(view,"None", Snackbar.LENGTH_LONG).show()
        }
    }

    /**
     * Method to show or hide the FloatingActionButton depending upon the NavigationController destination
     * @param controller not used
     * @param destination currently open Drawer Fragment, id tells which Fragment
     * @param arguments not used
     */
    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        val id = destination.id
        if (id == R.id.nav_camera || id == R.id.nav_settings || id == R.id.nav_wiki)
            fab.hide()
        else
            fab.show()
    }

    /**
     * Method used by the SettingsFragment.IFragmentToActivity Interface within the SettingsFragment. Sets the NavigationController destination to Schedule to exit the Fragment
     */
    override fun returnToSchedule() {
        navController.navigate(R.id.nav_schedule)
    }

    /**
     * Method used by the ScheduleFragment.IFragmentToActivity Interface within the ScheduleFragment. Sets the calendar's date when the CalendarView's date is changed.
     */
    override fun setCalendarDate(year: Int, month: Int, day: Int) {
        calendar.set(year, month, day)
    }

    /**
     * Method used to handle the AddSessionDialog output. Checks for conflicts, checks if a makeup session needs to be used and attempts to insert the new session
     * @param session Session object of the session that the user intends to add to their schedule
     * @param scheduleType ScheduleType of the session owner. Used to check if a makeup session needs to be used
     * @return true if no errors occurred, false if something goes wrong
     */
    private fun addSessionConfirm(session: Session, scheduleType: ScheduleType): Boolean{
        return if (!databaseOperations.checkSessionConflict(session, false)) {//checks if the new session conflicts an existing session
            if (scheduleType == ScheduleType.WEEKLY_CONSTANT) {
                if (!databaseOperations.removeCanceledSession(session.clientID)){//if the client's schedule is constant, attempt to remove a makeup session logged
                    Snackbar.make(fab,"SQL error removing canceled session from Session_Changes", Snackbar.LENGTH_LONG).show()
                    return false//exit with false if an error occurred. Don't add session
                }
            }
            if (databaseOperations.insertSession(session)){//if inserting the session is successful, prompt user, navigate to ScheduleFragment and return true
                Snackbar.make(fab,"Successfully inserted new session", Snackbar.LENGTH_LONG).show()
                navController.navigate(R.id.nav_schedule)
                true
            } else {
                Snackbar.make(fab,"SQL error inserting new session", Snackbar.LENGTH_LONG).show()
                false//exit with false if error occurred while inserting
            }
        } else {
            Toast.makeText(this,"Session conflict found", Toast.LENGTH_LONG).show()
            false//exit with false if conflict found
        }
    }

    /**
     * Method used to handle AddEditMuscleDialog output. Checks for conflicts with existing muscles and attempts to add muscle
     * @param muscle MuscleJoint object containing the data collected from the user
     * @return true if no errors or conflicts found
     */
    private fun addEditConfirm(muscle: MuscleJoint): Boolean = if (databaseOperations.checkMuscleConflict(muscle)) databaseOperations.addMuscle(muscle) else false
}
