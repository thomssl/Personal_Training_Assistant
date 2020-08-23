package com.trainingapp.trainingassistant.ui.programs

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.activities.AddEditProgramActivity
import com.trainingapp.trainingassistant.database.DatabaseOperations
import com.trainingapp.trainingassistant.objects.Program
import kotlinx.android.synthetic.main.fragment_programs.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Fragment called upon from 'Programs' action within the NavigationDrawer. Used to display, edit and delete programs
 */
class ProgramsFragment : Fragment(), CoroutineScope {

    private lateinit var databaseOperations: DatabaseOperations
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_programs, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        databaseOperations = DatabaseOperations(context)
    }

    override fun onResume() {
        super.onResume()
        //calls UI coroutine to get ExercisesRVAdapter
        setAdapter()
    }

    /**
     * UI coroutine to get and set rvPrograms adapter
     */
    private fun setAdapter() = launch{
        // Awaits IO coroutine to get adapter
        val result = getAdapter()
        // Displays adapter once coroutine has finished
        rvPrograms.adapter = result
        // Makes progress bar disappear once data received
        prgProgramData.visibility = View.GONE
    }

    /**
     * Suspendable IO coroutine to get an ProgramsRVAdapter for rvPrograms
     */
    private suspend fun getAdapter() : ProgramsRVAdapter = withContext(Dispatchers.IO){
        ProgramsRVAdapter(databaseOperations.getAllPrograms(), {id -> onItemClicked(id)}, {program -> onItemLongClicked(program) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
    }

    /**
     * Method passed to ProgramsRVAdapter to handle item onClick event. Opens AddEditProgramActivity with the id of the program to be edited
     * @param id id of the program clicked, from adapter
     */
    private fun onItemClicked(id: Int){
        val intent = Intent(context, AddEditProgramActivity::class.java)
        intent.putExtra("id", id)
        startActivity(intent)
    }

    /**
     * Method passed to ProgramsRVAdapter to handle item onLongClick event. Opens AlertDialog to make user confirm program deletion
     * @param program Program object to be removed from the database, from adapter
     * @return always true since the callback consumed the long click (See Android View.onLongClickListener for more info)
     */
    private fun onItemLongClicked(program: Program): Boolean{
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle(getString(R.string.alert_dialog_confirm_removal))
        alertDialog.setMessage(getString(R.string.confirm_delete, program.name))
        alertDialog.setPositiveButton(R.string.confirm) { _, _ -> databaseOperations.removeProgram(program); setAdapter()}
        alertDialog.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss()}
        alertDialog.show()
        return true
    }
}