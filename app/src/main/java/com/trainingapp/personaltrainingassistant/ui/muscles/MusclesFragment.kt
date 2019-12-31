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
        setAdapter()
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
    }

    private fun setAdapter() = launch{
        val result = getAdapter()
        rvMuscles.adapter = result
        prgMusclesData.visibility = View.GONE
    }

    private suspend fun getAdapter(): MusclesRVAdapter = withContext(Dispatchers.IO){
        MusclesRVAdapter(databaseOperations.getAllMuscles(), {muscleJoint -> onItemClick(muscleJoint) }, {muscleJoint, view -> onItemLongClick(muscleJoint, view) })
    }

    private fun onItemClick(muscleJoint: MuscleJoint){
        val dialog = AddEditMuscleDialog(muscleJoint) {muscleJointDialog, _ -> editConfirm(muscleJointDialog) }
        dialog.show(fragmentManager, "Edit Exercise")
    }

    private fun editConfirm(muscleJoint: MuscleJoint): Boolean = if (databaseOperations.checkMuscleConflict(muscleJoint)) databaseOperations.updateMuscle(muscleJoint) else false

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