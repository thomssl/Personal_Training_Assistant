package com.trainingapp.trainingassistant.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.trainingapp.trainingassistant.R
import com.trainingapp.trainingassistant.StaticFunctions
import com.trainingapp.trainingassistant.database.DatabaseOperations
import com.trainingapp.trainingassistant.enumerators.ExerciseType
import com.trainingapp.trainingassistant.objects.Exercise
import com.trainingapp.trainingassistant.objects.MuscleJoint
import com.trainingapp.trainingassistant.ui.adapters.AddExercisePrimaryRVAdapter
import com.trainingapp.trainingassistant.ui.adapters.AddExerciseSecondaryRVAdapter
import com.trainingapp.trainingassistant.ui.adapters.EditExercisePrimaryRVAdapter
import com.trainingapp.trainingassistant.ui.adapters.EditExerciseSecondaryRVAdapter
import kotlinx.android.synthetic.main.activity_add_edit_exercise.*

/**
 * Activity to add or edit an exercise. Will always check for an id sent via Intent (default is 0)
 * If the id > 0 (ie a valid id) the layout will be populated with the exercises data
 * If the id <= 0 the layout will be left blank for the user to input new values
 * After collecting the data from the user, the exercise is added or updated depending upon the isNew flag
 */
class AddEditExerciseActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var databaseOperations: DatabaseOperations
    private var muscles = ArrayList<MuscleJoint>()
    private var joints = ArrayList<MuscleJoint>()
    private lateinit var exercise: Exercise
    private var isNew = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_exercise)
        setTitle(R.string.edit_exercise_activity)//default title is "Edit Exercise"

        databaseOperations = DatabaseOperations(this)
        muscles = databaseOperations.getAllMuscles()
        joints = databaseOperations.getAllJoints()
        exercise = databaseOperations.getExercise(intent.getIntExtra("id", 0))//get Exercise from id. If no exercise is found (ie invalid id) a blank exercise is returned with an id of 0
        val temp = ArrayList<MuscleJoint>()//used to hold the possible secondary movers with the primary mover chosen removed. Only used if editing an exercise
        isNew = exercise.id == 0//sets isNew flag based upon the id of the returned Exercise id
        if (isNew){//if creating a new exercise
            setTitle(R.string.add_exercise_activity)//change title of activity
            exercise.type = ExerciseType.STRENGTH//set Exercise object's exercise type as the default
        } else {//if editing an existing exercise
            if (exercise.type == ExerciseType.STRENGTH)//fill the temp ArrayList depending upon the exercise type
                muscles.forEach { temp.add(it) }
            else
                joints.forEach { temp.add(it) }
            temp.remove(exercise.primaryMover)//remove the primary mover from the list to remove redundant data
        }
        spnAddEditExerciseType.adapter = ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, StaticFunctions.getExerciseTypeNameArray())//fill spinner with exercise type options
        spnAddEditExerciseType.onItemSelectedListener = this
        spnAddEditExerciseType.setSelection(exercise.type.num - 1)//the exercise type num corresponds to the spinner index + 1
        etxtAddEditExerciseName.setText(exercise.name)
        if (exercise.type == ExerciseType.STRENGTH){//if the exercise type is Strength, fill the adapter(s) with muscle information
            if (!isNew)//if is edit exercise, populate secondary movers with temp ArrayList and send secondary movers already selected
                rvAddEditExerciseSecondaryMover.adapter = EditExerciseSecondaryRVAdapter(temp, exercise.getLstSecondaryMovers()) { muscleJoint, isSelected -> onSecondaryItemClick(muscleJoint, isSelected) }
            rvAddEditExercisePrimeMover.adapter = EditExercisePrimaryRVAdapter(muscles, muscles.indexOf(exercise.primaryMover)) { muscleJoint -> onPrimaryItemClick(muscleJoint) }
        } else {//if the exercise type is Mobility or Stability, fill the adapter(s) with joint information
            if (!isNew)//if it is edit exercise, populate secondary movers with temp ArrayList and send secondary movers already selected
                rvAddEditExerciseSecondaryMover.adapter = EditExerciseSecondaryRVAdapter(temp, exercise.getLstSecondaryMovers()) { muscleJoint, isSelected -> onSecondaryItemClick(muscleJoint, isSelected) }
            rvAddEditExercisePrimeMover.adapter = EditExercisePrimaryRVAdapter(joints, joints.indexOf(exercise.primaryMover)) { muscleJoint -> onPrimaryItemClick(muscleJoint) }
        }
    }

    /**
     * Method to handle the confirm button's onClick event. When clicked, data will be collected and if the data is valid the exercise will be updated/added depending upon thr isNew flag
     * @param view Button View that this function is associated with (btnAddEditExerciseConfirm)
     */
    fun onConfirmClick(view: View){
        val name = etxtAddEditExerciseName.text.toString()
        when (true) {
            //checks if the name has invalid character or is blank
            StaticFunctions.badSQLText(name) -> Snackbar.make(view, "Invalid input character inside exercise name. See Wiki for more information", Snackbar.LENGTH_LONG).show()
            exercise.primaryMover.id == 0 -> Snackbar.make(view, "No primary mover selected", Snackbar.LENGTH_LONG).show()
            else -> {//if passes all tests
                exercise.name = name
                if (!databaseOperations.checkExerciseConflict(exercise)) {//if no conflict is found with the existing exercises
                    if (isNew) {
                        if (databaseOperations.insertExercise(exercise)) {
                            Snackbar.make(view, "Inserted new exercise", Snackbar.LENGTH_LONG).show()
                            finish()
                        } else
                            Snackbar.make(view, "SQL Error inserting new exercise", Snackbar.LENGTH_LONG).show()
                    } else {
                        if (databaseOperations.updateExercise(exercise)) {
                            Snackbar.make(view, "Updated exercise", Snackbar.LENGTH_LONG).show()
                            finish()
                        } else
                            Snackbar.make(view, "SQL Error updating exercise", Snackbar.LENGTH_LONG).show()
                    }
                } else
                    Snackbar.make(view, "Conflict with existing exercise", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        if (position != exercise.type.num - 1) {//if the exercise type selected has changed
            exercise.primaryMover = MuscleJoint(0,"")//clear primary mover
            exercise.clearSecondaryMovers()//clear secondary movers
            when (position) {//depending upon the item selected, the exercise type will be assigned and the primary movers RecyclerView will be populated with the appropriate data
                0 -> {
                    exercise.type = ExerciseType.STRENGTH
                    rvAddEditExercisePrimeMover.adapter = AddExercisePrimaryRVAdapter(muscles) { muscleJoint -> onPrimaryItemClick(muscleJoint)}
                }
                1 -> {
                    exercise.type = ExerciseType.MOBILITY
                    rvAddEditExercisePrimeMover.adapter = AddExercisePrimaryRVAdapter(joints) { muscleJoint -> onPrimaryItemClick(muscleJoint)}
                }
                2 -> {
                    exercise.type = ExerciseType.STABILITY
                    rvAddEditExercisePrimeMover.adapter = AddExercisePrimaryRVAdapter(joints) { muscleJoint -> onPrimaryItemClick(muscleJoint)}
                }
            }
            rvAddEditExerciseSecondaryMover.adapter = AddExerciseSecondaryRVAdapter(ArrayList()) { muscleJoint, isSelected -> onSecondaryItemClick(muscleJoint, isSelected) }//in all cases the secondary movers RecyclerView will be assigned to blank
        }
    }

    /**
     * Method sent to the primary mover RecyclerView to handle the onItemClick event. Used to assign Exercise's primary mover from selected item and populate secondary movers RecyclerView
     * @param muscleJoint Object containing the data on the selected primary mover
     */
    private fun onPrimaryItemClick(muscleJoint: MuscleJoint){
        exercise.primaryMover = muscleJoint
        val temp = ArrayList<MuscleJoint>()//temp ArrayList to be sent to the secondary movers RecyclerView. Will be populated with the muscle or joint data with respect to the exercise type
        if (exercise.type == ExerciseType.STRENGTH)
            muscles.forEach { temp.add(it) }
        else
            joints.forEach { temp.add(it) }
        temp.remove(muscleJoint)
        rvAddEditExerciseSecondaryMover.adapter = AddExerciseSecondaryRVAdapter(temp) { muscleJointAdapter, isSelected -> onSecondaryItemClick(muscleJointAdapter, isSelected) }
    }

    /**
     * Method sent to the secondary movers RecyclerView to handle the onItemCLick event. If the item was selected the secondary mover will be added to the exercise.
     * If the item was deselected the secondary mover will be removed from the exercise
     * @param muscleJoint Object containing the data on the selected/deselected secondary mover
     * @param isSelected flag denoting the state of the item clicked
     */
    private fun onSecondaryItemClick(muscleJoint: MuscleJoint, isSelected: Boolean){
        if (isSelected)
            exercise.addSecondaryMover(muscleJoint)
        else
            exercise.removeSecondaryMover(muscleJoint)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        //nothing needs to happen here
    }
}
