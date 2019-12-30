package com.trainingapp.personaltrainingassistant.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
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
import com.trainingapp.personaltrainingassistant.ui.settings.SettingsFragment
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_schedule.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener, NavController.OnDestinationChangedListener, SettingsFragment.IFragmentToActivity {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var databaseOperations: DatabaseOperations

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

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

    private fun loadPreBuiltDatabase() {
        val file = File(getString(R.string.filePath))
        if (!file.exists()) {
            val assetManager = assets
            try {
                val `in` = assetManager.open("data.db")
                val out: OutputStream =
                    FileOutputStream(getString(R.string.filePath))
                val buffer = ByteArray(1024)
                var read = `in`.read(buffer)
                while (read != -1) {
                    out.write(buffer, 0, read)
                    read = `in`.read(buffer)
                }
            } catch (ex: IOException) {
                Log.d("here", ex.message)
            }
        } else {
            Log.d("here", "File Exists")
        }
    }

    override fun onClick(view: View) {
        when (navController.currentDestination!!.id){
            R.id.nav_schedule -> {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = calSchedule.date
                val dialog = AddSessionDialog(databaseOperations.getAddSessionsClientsByDay(calendar), calendar) { session, scheduleType -> addSessionConfirm(session,scheduleType) }
                dialog.show(supportFragmentManager, "Add Session")
            }
            R.id.nav_clients -> {
                val intent = Intent(this, AddEditClientActivity::class.java)
                intent.putExtra("id", 0)
                startActivity(intent)
            }
            R.id.nav_exercises -> {
                val intent = Intent(this, AddEditExerciseActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_camera -> {
                Snackbar.make(view,"Name: Camera", Snackbar.LENGTH_LONG).show()
            }
            R.id.nav_muscles -> {
                val dialog = AddEditMuscleDialog(MuscleJoint(0, "")) {muscleJoint, b -> addEditConfirm(muscleJoint, b) }
                dialog.show(supportFragmentManager, "Add Muscle")
            }
            else -> Snackbar.make(view,"None", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        val id = destination.id
        if (id == R.id.nav_camera || id == R.id.nav_settings || id == R.id.nav_wiki)
            fab.hide()
        else
            fab.show()
    }

    override fun communicateToActivity() {
        navController.navigate(R.id.nav_schedule)
    }

    private fun addSessionConfirm(session: Session, scheduleType: ScheduleType): Boolean{
        return if (!databaseOperations.checkSessionConflict(session, false)) {
            if (scheduleType == ScheduleType.WEEKLY_CONSTANT) {
                if (!databaseOperations.removeCanceledSession(session.clientID)){
                    Snackbar.make(fab,"SQL error removing canceled session from Session_Changes", Snackbar.LENGTH_LONG).show()
                    return false
                }
            }
            if (databaseOperations.insertSession(session)){
                Snackbar.make(fab,"Successfully inserted new session", Snackbar.LENGTH_LONG).show()
                navController.navigate(R.id.nav_schedule)
                true
            } else {
                Snackbar.make(fab,"SQL error inserting new session", Snackbar.LENGTH_LONG).show()
                false
            }
        } else {
            Snackbar.make(fab,"Session conflict found", Snackbar.LENGTH_LONG).show()
            false
        }
    }

    private fun addEditConfirm(muscleJoint: MuscleJoint, isNew: Boolean): Boolean{
        return if (isNew)
            databaseOperations.addMuscle(muscleJoint)
        else
            databaseOperations.updateMuscle(muscleJoint)
    }
}
