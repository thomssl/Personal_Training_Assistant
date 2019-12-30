package com.trainingapp.personaltrainingassistant.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.material.snackbar.Snackbar
import com.trainingapp.personaltrainingassistant.database.DatabaseOperations
import com.trainingapp.personaltrainingassistant.R
import com.trainingapp.personaltrainingassistant.StaticFunctions
import com.trainingapp.personaltrainingassistant.enumerators.ExerciseType
import com.trainingapp.personaltrainingassistant.objects.Exercise
import com.trainingapp.personaltrainingassistant.objects.MuscleJoint
import com.trainingapp.personaltrainingassistant.ui.adapters.AddExercisePrimaryRVAdapter
import com.trainingapp.personaltrainingassistant.ui.adapters.AddExerciseSecondaryRVAdapter
import com.trainingapp.personaltrainingassistant.ui.adapters.EditExercisePrimaryRVAdapter
import com.trainingapp.personaltrainingassistant.ui.adapters.EditExerciseSecondaryRVAdapter
import kotlinx.android.synthetic.main.activity_add_edit_exercise.*

class AddEditExerciseActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var databaseOperations: DatabaseOperations
    private var muscles = ArrayList<MuscleJoint>()
    private var joints = ArrayList<MuscleJoint>()
    private val muscleNames = ArrayList<String>()
    private val jointNames = ArrayList<String>()
    private lateinit var exercise: Exercise
    private var isAdd = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_exercise)
        setTitle(R.string.edit_exercise_activity)

        databaseOperations = DatabaseOperations(this)
        muscles = databaseOperations.getAllMuscles()
        muscles.forEach{muscleNames.add(it.name)}
        joints = databaseOperations.getAllJoints()
        joints.forEach{jointNames.add(it.name)}
        exercise = databaseOperations.getExercise(intent.getIntExtra("id", 0))
        val temp = ArrayList<MuscleJoint>()
        isAdd = exercise.id == 0
        if (isAdd){
            setTitle(R.string.add_exercise_activity)
            exercise.type = ExerciseType.STRENGTH
        } else {
            if (exercise.type == ExerciseType.STRENGTH)
                muscles.forEach { temp.add(it) }
            else
                joints.forEach { temp.add(it) }
            temp.remove(MuscleJoint(exercise.primaryMover, exercise.strPrimaryMover))
        }
        spnAddEditExerciseType.adapter = ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, StaticFunctions.getExerciseTypeNameArray())
        spnAddEditExerciseType.onItemSelectedListener = this
        spnAddEditExerciseType.setSelection(exercise.type.value - 1)
        etxtAddEditExerciseName.setText(exercise.name)
        if (exercise.type == ExerciseType.STRENGTH){
            if (!isAdd)
                rvAddEditExerciseSecondaryMover.adapter = EditExerciseSecondaryRVAdapter(temp, exercise.strSecondaryMoversList) { muscleJoint, isSelected -> onSecondaryItemClick(muscleJoint, isSelected) }
            rvAddEditExercisePrimeMover.adapter = EditExercisePrimaryRVAdapter(muscles, muscleNames.indexOf(exercise.strPrimaryMover)) { muscleJoint -> onPrimaryItemClick(muscleJoint) }
        } else {
            if (!isAdd)
                rvAddEditExerciseSecondaryMover.adapter = EditExerciseSecondaryRVAdapter(temp, exercise.strSecondaryMoversList) { muscleJoint, isSelected -> onSecondaryItemClick(muscleJoint, isSelected) }
            rvAddEditExercisePrimeMover.adapter = EditExercisePrimaryRVAdapter(joints, jointNames.indexOf(exercise.strPrimaryMover)) { muscleJoint -> onPrimaryItemClick(muscleJoint) }
        }
    }

    fun onConfirmClick(view: View){
        val name = etxtAddEditExerciseName.text.toString()
        when (true){
            StaticFunctions.badSQLText(name) -> Snackbar.make(view, "Invalid input character inside exercise name. See Wiki for more information", Snackbar.LENGTH_LONG).show()
            else -> {
                if (isAdd) {
                    if (databaseOperations.checkExerciseConflict(exercise)) {
                        if (databaseOperations.insertExercise(exercise)) {
                            Snackbar.make(view, "Inserted new exercise", Snackbar.LENGTH_LONG).show()
                            finish()
                        } else
                            Snackbar.make(view, "SQL Error inserting new exercise", Snackbar.LENGTH_LONG).show()
                    } else
                        Snackbar.make(view, "Conflict with existing exercise", Snackbar.LENGTH_LONG).show()
                } else {
                    if(databaseOperations.updateExercise(exercise)) {
                        Snackbar.make(view, "Updated exercise", Snackbar.LENGTH_LONG).show()
                        finish()
                    }
                    else
                        Snackbar.make(view, "SQL Error updating exercise", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        if (position != exercise.type.value - 1) {
            exercise.primaryMover = 0
            exercise.strPrimaryMover = ""
            exercise.clearSecondaryMovers()
            when (position) {
                0 -> {
                    exercise.type = ExerciseType.STRENGTH
                    rvAddEditExerciseSecondaryMover.adapter = AddExerciseSecondaryRVAdapter(ArrayList()) { muscleJoint, isSelected -> onSecondaryItemClick(muscleJoint, isSelected) }
                    rvAddEditExercisePrimeMover.adapter = AddExercisePrimaryRVAdapter(muscles) { muscleJoint -> onPrimaryItemClick(muscleJoint)}
                }
                1 -> {
                    exercise.type = ExerciseType.MOBILITY
                    rvAddEditExerciseSecondaryMover.adapter = AddExerciseSecondaryRVAdapter(ArrayList()) { muscleJoint, isSelected -> onSecondaryItemClick(muscleJoint, isSelected) }
                    rvAddEditExercisePrimeMover.adapter = AddExercisePrimaryRVAdapter(joints) { muscleJoint -> onPrimaryItemClick(muscleJoint)}
                }
                2 -> {
                    exercise.type = ExerciseType.STABILITY
                    rvAddEditExerciseSecondaryMover.adapter = AddExerciseSecondaryRVAdapter(ArrayList()) { muscleJoint, isSelected -> onSecondaryItemClick(muscleJoint, isSelected) }
                    rvAddEditExercisePrimeMover.adapter = AddExercisePrimaryRVAdapter(joints) { muscleJoint -> onPrimaryItemClick(muscleJoint)}
                }
            }
        }
    }

    private fun onPrimaryItemClick(muscleJoint: MuscleJoint){
        exercise.primaryMover = muscleJoint.id
        exercise.strPrimaryMover = muscleJoint.name
        val temp = ArrayList<MuscleJoint>()
        if (exercise.type == ExerciseType.STRENGTH)
            muscles.forEach { temp.add(it) }
        else
            joints.forEach { temp.add(it) }
        temp.remove(muscleJoint)
        rvAddEditExerciseSecondaryMover.adapter = AddExerciseSecondaryRVAdapter(temp) { muscleJointAdapter, isSelected -> onSecondaryItemClick(muscleJointAdapter, isSelected) }
    }

    private fun onSecondaryItemClick(muscleJoint: MuscleJoint, isSelected: Boolean){
        if (isSelected) exercise.addSecondaryMover(muscleJoint.id, muscleJoint.name) else exercise.removeSecondaryMover(muscleJoint.id, muscleJoint.name)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
