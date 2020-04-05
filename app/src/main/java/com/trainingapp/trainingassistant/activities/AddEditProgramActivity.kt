package com.trainingapp.trainingassistant.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.database.DatabaseOperations
import com.trainingapp.trainingassistant.objects.ExerciseProgram
import com.trainingapp.trainingassistant.objects.Program
import com.trainingapp.trainingassistant.ui.adapters.ProgramExercisesRVAdapter
import com.trainingapp.trainingassistant.ui.dialogs.AddExerciseProgramDialog
import com.trainingapp.trainingassistant.ui.dialogs.EditExerciseProgramDialog
import kotlinx.android.synthetic.main.activity_add_edit_program.*

class AddEditProgramActivity : AppCompatActivity() {

    private lateinit var databaseOperations: DatabaseOperations
    private var isNew = false
    private lateinit var intentProgram: Program

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_program)
        setTitle(R.string.edit_program_title)//default is edit client, changed if invalid client id sent

        databaseOperations = DatabaseOperations(this)
        val id = intent.getIntExtra("id", 0)
        intentProgram = databaseOperations.getProgram(id)
        isNew = intentProgram.id == 0
        if (isNew){
            setTitle(R.string.add_program_title)
        } else {
            etxtAddEditProgramName.setText(intentProgram.name)
            etxtAddEditProgramDesc.setText(intentProgram.desc)
            spnAddEditProgramDays.setSelection(intentProgram.days-1)
            updateAdapter()
        }
    }

    fun onClickBtnAddExercise(view: View){
        val addExerciseDialog = AddExerciseProgramDialog { dialog -> addExerciseDialogListener(dialog) }
        addExerciseDialog.show(supportFragmentManager, "Add Program Exercise")
        updateAdapter()
    }

    fun onClickBtnConfirm(view: View){
        //do some confirming stuff
    }

    private fun updateAdapter(){
        rvProgramExercises.adapter = ProgramExercisesRVAdapter(
            intentProgram,
            { exerciseProgram, id -> onItemClick(exerciseProgram, id) },
            { exerciseProgram ->  deleteExerciseListener(exerciseProgram)}
        )
    }

    private fun onItemClick(exerciseProgram: ExerciseProgram, position: Int){
        val editExerciseDialog = EditExerciseProgramDialog(exerciseProgram, position) { dialog, i -> editExerciseDialogListener(dialog,i) }
        editExerciseDialog.show(supportFragmentManager, "Edit Exercise")
    }

    private fun addExerciseDialogListener(dialog: AddExerciseProgramDialog): Boolean{
        return true
    }

    private fun editExerciseDialogListener(dialog: EditExerciseProgramDialog, position: Int): Boolean{
        return true
    }

    private fun deleteExerciseListener(exerciseProgram: ExerciseProgram): Boolean{
        return true
    }
}
