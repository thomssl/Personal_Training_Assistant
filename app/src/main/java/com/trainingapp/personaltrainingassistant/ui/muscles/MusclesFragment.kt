package com.trainingapp.personaltrainingassistant.ui.muscles

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.trainingapp.personaltrainingassistant.database.DatabaseOperations
import com.trainingapp.personaltrainingassistant.objects.MuscleJoint
import com.trainingapp.personaltrainingassistant.R
import com.trainingapp.personaltrainingassistant.ui.dialogs.AddEditMuscleDialog
import kotlinx.android.synthetic.main.fragment_muscles.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Fragment called upon from 'Muscles' action within the NavigationDrawer. Used to display, edit and delete muscles
 */
class MusclesFragment : Fragment(), CoroutineScope {

    private lateinit var databaseOperations: DatabaseOperations
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override fun onAttach(context: Context) {
        super.onAttach(context)
        databaseOperations = DatabaseOperations(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_muscles, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()//calls UI coroutine to get MusclesRVAdapter
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
    }

    /**
     * UI coroutine to get and set rvMuscles adapter
     */
    private fun setAdapter() = launch{
        val result = getAdapter()//awaits IO coroutine to get adapter
        rvMuscles.adapter = result//displays adapter once coroutine has finished
        prgMusclesData.visibility = View.GONE//makes progress bar disappear once data received
    }

    /**
     * Suspendable IO coroutine to get an MusclesRVAdapter for rvMuscles
     */
    private suspend fun getAdapter(): MusclesRVAdapter = withContext(Dispatchers.IO){
        MusclesRVAdapter(databaseOperations.getAllMuscles(), {muscleJoint -> onItemClick(muscleJoint) }, {muscleJoint, view -> onItemLongClick(muscleJoint, view) })
    }

    /**
     * Method passed to MusclesRVAdapter to handle item onClick event. Opens AddEditMuscleDialog with the MuscleJoint object of the muscle to be edited
     * @param muscleJoint MuscleJoint object filled with data for muscle to be updated, from adapter
     */
    private fun onItemClick(muscleJoint: MuscleJoint){
        val dialog = AddEditMuscleDialog(muscleJoint) {muscleJointDialog -> editConfirm(muscleJointDialog) }
        dialog.show(fragmentManager, "Edit Exercise")
    }

    /**
     * Method passed to AddEditMuscleDialog to handle confirm button onClick event. Checks for conflict and adds muscle if no conflict found
     * @param muscleJoint MuscleJoint object filled with data for muscle to be updated, from dialog
     * @return true if no conflict and update was successful, false if conflict or error updating muscle
     */
    private fun editConfirm(muscleJoint: MuscleJoint): Boolean = if (databaseOperations.checkMuscleConflict(muscleJoint)) databaseOperations.updateMuscle(muscleJoint) else false

    /**
     * Method passed to MusclesRVAdapter to handle item onLongClick event. Opens AlertDialog to make user confirm exercise deletion
     * @param muscleJoint MuscleJoint object to be removed from the database, from adapter
     * @return always true since the callback consumed the long click (See Android View.onLongClickListener for more info)
     */
    private fun onItemLongClick(muscleJoint: MuscleJoint, view: View): Boolean{
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle(getString(R.string.confirm_delete, muscleJoint.name))
        alertDialog.setPositiveButton(R.string.confirm) { _, _ ->
            when (databaseOperations.removeMuscle(muscleJoint)){
                0 -> Snackbar.make(view, "SQL error deleting ${muscleJoint.name}", Snackbar.LENGTH_LONG).show()
                1 -> {Snackbar.make(view, "${muscleJoint.name} successfully deleted", Snackbar.LENGTH_LONG).show(); setAdapter()}
                2 -> Snackbar.make(view, "${muscleJoint.name} is used to define an exercise. Delete that exercise in order to remove the muscle", Snackbar.LENGTH_LONG).show()
            }
        }
        alertDialog.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss()}
        alertDialog.show()
        return true
    }
}