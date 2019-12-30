package com.trainingapp.personaltrainingassistant.ui.exercises

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.trainingapp.personaltrainingassistant.database.DatabaseOperations
import com.trainingapp.personaltrainingassistant.objects.Exercise
import com.trainingapp.personaltrainingassistant.R
import com.trainingapp.personaltrainingassistant.activities.AddEditExerciseActivity
import kotlinx.android.synthetic.main.fragment_exercises.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

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
        setAdapter()
    }

    private fun setAdapter() = launch{
        val result = getAdapter()
        rvExercises.adapter = result
        prgExerciseData.visibility = View.GONE
    }

    private suspend fun getAdapter() : ExercisesRVAdapter = withContext(Dispatchers.IO){
        ExercisesRVAdapter(databaseOperations.getAllExercises(), {id -> onItemClicked(id)}, {exercise -> onItemLongClicked(exercise) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
    }

    private fun onItemClicked(id: Int){
        val intent = Intent(context, AddEditExerciseActivity::class.java)
        intent.putExtra("id", id)
        startActivity(intent)
    }

    private fun onItemLongClicked(exercise: Exercise): Boolean{
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle(getString(R.string.confirm_delete, exercise.name))
        alertDialog.setPositiveButton(R.string.confirm) { _, _ -> databaseOperations.removeExercise(exercise); setAdapter()}
        alertDialog.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss()}
        alertDialog.show()
        return true
    }
}