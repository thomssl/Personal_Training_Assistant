package com.trainingapp.trainingassistant.ui.exercises

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.activities.AddEditExerciseActivity
import com.trainingapp.trainingassistant.database.DatabaseOperations
import com.trainingapp.trainingassistant.objects.Exercise
import kotlinx.android.synthetic.main.fragment_exercises.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Fragment called upon from 'Exercises' action within the NavigationDrawer. Used to display, edit and delete exercises
 */
class ExercisesFragment : Fragment(), CoroutineScope {

    private lateinit var databaseOperations: DatabaseOperations
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_exercises, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        databaseOperations = DatabaseOperations(context)
    }

    override fun onResume() {
        super.onResume()
        setAdapter()//calls UI coroutine to get ExercisesRVAdapter
    }

    /**
     * UI coroutine to get and set rvExercises adapter
     */
    private fun setAdapter() = launch{
        val result = getAdapter()//awaits IO coroutine to get adapter
        rvExercises.adapter = result//displays adapter once coroutine has finished
        prgExerciseData.visibility = View.GONE//makes progress bar disappear once data received
    }

    /**
     * Suspendable IO coroutine to get an ExercisesRVAdapter for rvExercises
     */
    private suspend fun getAdapter() : ExercisesRVAdapter = withContext(Dispatchers.IO){
        ExercisesRVAdapter(databaseOperations.getAllExercises(), {id -> onItemClicked(id)}, {exercise -> onItemLongClicked(exercise) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
    }

    /**
     * Method passed to ExercisesRVAdapter to handle item onClick event. Opens AddEditExerciseActivity with the id of the exercise to be edited
     * @param id id of the exercise clicked, from adapter
     */
    private fun onItemClicked(id: Int){
        val intent = Intent(context, AddEditExerciseActivity::class.java)
        intent.putExtra("id", id)
        startActivity(intent)
    }

    /**
     * Method passed to ExercisesRVAdapter to handle item onLongClick event. Opens AlertDialog to make user confirm exercise deletion
     * @param exercise Exercise object to be removed from the database, from adapter
     * @return always true since the callback consumed the long click (See Android View.onLongClickListener for more info)
     */
    private fun onItemLongClicked(exercise: Exercise): Boolean{
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle(getString(R.string.alert_dialog_confirm_removal))
        alertDialog.setMessage(getString(R.string.confirm_delete, exercise.name))
        alertDialog.setPositiveButton(R.string.confirm) { _, _ -> databaseOperations.removeExercise(exercise); setAdapter()}
        alertDialog.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss()}
        alertDialog.show()
        return true
    }
}