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
    private lateinit var muscles: MutableList<MuscleJoint>
    private lateinit var joints: MutableList<MuscleJoint>
    private lateinit var exercise: Exercise
    private var isNew = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_exercise)
        //default title is "Edit Exercise"
        setTitle(R.string.edit_exercise_activity)

        databaseOperations = DatabaseOperations(this)
        muscles = databaseOperations.getAllMuscles()
        joints = databaseOperations.getAllJoints()
        // Get Exercise from id. If no exercise is found (ie invalid id) a blank exercise is returned with an id of 0
        exercise = databaseOperations.getExercise(intent.getIntExtra("id", 0))
        // Sets isNew flag based upon the id of the returned Exercise id
        isNew = exercise.id == 0
        // If creating a new exercise
        if (isNew){
            // Change title of activity
            setTitle(R.string.add_exercise_activity)
            // Set Exercise object's exercise type as the default
            exercise.type = ExerciseType.STRENGTH
        }
        // Fill spinner with exercise type options
        spnAddEditExerciseType.adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_expandable_list_item_1,
            StaticFunctions.getExerciseTypeNameArray()
        )
        spnAddEditExerciseType.onItemSelectedListener = this
        // The exercise type num corresponds to the spinner index + 1
        spnAddEditExerciseType.setSelection(exercise.type.num - 1)
        etxtAddEditExerciseName.setText(exercise.name)
        // If the exercise type is Strength, fill the adapter(s) with muscle information
        if (exercise.type == ExerciseType.STRENGTH) {
            if (!isNew) {
                // If is edit exercise, populate secondary movers with temp list and send secondary movers already selected
                val temp = muscles.filter { it != exercise.primaryMover }
                rvAddEditExerciseSecondaryMover.adapter = EditExerciseSecondaryRVAdapter(temp, exercise.getLstSecondaryMovers()) {
                        muscleJoint, isSelected -> onSecondaryItemClick( muscleJoint, isSelected )
                }
            }
            rvAddEditExercisePrimeMover.adapter = EditExercisePrimaryRVAdapter(muscles, muscles.indexOf(exercise.primaryMover)) {
                    muscleJoint -> onPrimaryItemClick(muscleJoint)
            }
        } else {
            if (!isNew) {
                // If it is edit exercise, populate secondary movers with temp list and send secondary movers already selected
                val temp = joints.filter { it != exercise.primaryMover }
                rvAddEditExerciseSecondaryMover.adapter = EditExerciseSecondaryRVAdapter(temp, exercise.getLstSecondaryMovers()) {
                        muscleJoint, isSelected -> onSecondaryItemClick(muscleJoint, isSelected)
                }
            }
            rvAddEditExercisePrimeMover.adapter = EditExercisePrimaryRVAdapter(joints, joints.indexOf(exercise.primaryMover)) {
                    muscleJoint -> onPrimaryItemClick(muscleJoint)
            }
        }
    }

    /**
     * Method to handle the confirm button's onClick event. When clicked, data will be collected and if the data is valid the exercise will be
     * updated/added depending upon thr isNew flag
     * @param view Button View that this function is associated with (btnAddEditExerciseConfirm)
     */
    fun onConfirmClick(view: View) {
        val name = etxtAddEditExerciseName.text.toString()
        when (true) {
            // Checks if the name has invalid character or is blank
            StaticFunctions.badSQLText(name) ->
                Snackbar.make(
                    view,
                    "Invalid input character inside exercise name. See Wiki for more information",
                    Snackbar.LENGTH_LONG
                ).show()
            exercise.primaryMover.id == 0 ->
                Snackbar.make(
                    view,
                    "No primary mover selected",
                    Snackbar.LENGTH_LONG
                ).show()
            // If new exercises passes all tests
            else -> {
                exercise.name = name
                //if no conflict is found with an existing exercise
                if (!databaseOperations.checkExerciseConflict(exercise)) {
                    if (isNew) {
                        if (databaseOperations.insertExercise(exercise)) {
                            Snackbar.make(
                                view,
                                "Inserted new exercise",
                                Snackbar.LENGTH_LONG
                            ).show()
                            finish()
                        } else
                            Snackbar.make(
                                view,
                                "SQL Error inserting new exercise",
                                Snackbar.LENGTH_LONG
                            ).show()
                    } else {
                        if (databaseOperations.updateExercise(exercise)) {
                            Snackbar.make(
                                view,
                                "Updated exercise",
                                Snackbar.LENGTH_LONG
                            ).show()
                            finish()
                        } else
                            Snackbar.make(
                                view,
                                "SQL Error updating exercise",
                                Snackbar.LENGTH_LONG
                            ).show()
                    }
                } else
                    Snackbar.make(
                        view,
                        "Conflict with existing exercise",
                        Snackbar.LENGTH_LONG
                    ).show()
            }
        }
    }

    /**
     * Method to handle item selection change for spnAddEditExerciseType. Checks to confirm that the selection has changed using internal variable
     * then resets exercises mover info and sets the proper adapters for primary and secondary movers
     * @param p0 not used
     * @param p1 not used
     * @param position current position of the spinner
     * @param p3 not used
     */
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        // If the exercise type selected has changed
        if (position != exercise.type.num - 1) {
            // Clear primary mover
            exercise.primaryMover = MuscleJoint.empty
            // Clear secondary movers
            exercise.clearSecondaryMovers()
            // Depending upon the item selected, the exercise type will be assigned and the primary movers RecyclerView will be populated
            when (position) {
                0 -> {
                    exercise.type = ExerciseType.STRENGTH
                    rvAddEditExercisePrimeMover.adapter = AddExercisePrimaryRVAdapter(muscles) {
                            muscleJoint -> onPrimaryItemClick(muscleJoint)
                    }
                }
                1 -> {
                    exercise.type = ExerciseType.MOBILITY
                    rvAddEditExercisePrimeMover.adapter = AddExercisePrimaryRVAdapter(joints) {
                            muscleJoint -> onPrimaryItemClick(muscleJoint)
                    }
                }
                2 -> {
                    exercise.type = ExerciseType.STABILITY
                    rvAddEditExercisePrimeMover.adapter = AddExercisePrimaryRVAdapter(joints) {
                            muscleJoint -> onPrimaryItemClick(muscleJoint)
                    }
                }
            }
            // In all cases the secondary movers RecyclerView will be assigned to blank
            rvAddEditExerciseSecondaryMover.adapter = AddExerciseSecondaryRVAdapter(listOf()) {
                    muscleJoint, isSelected -> onSecondaryItemClick(muscleJoint, isSelected)
            }
        }
    }

    /**
     * Method sent to the primary mover RecyclerView to handle the onItemClick event. Used to assign Exercise's primary mover from selected item and
     * populate secondary movers RecyclerView
     * @param muscleJoint Object containing the data on the selected primary mover
     */
    private fun onPrimaryItemClick(muscleJoint: MuscleJoint) {
        exercise.primaryMover = muscleJoint
        val temp = if (exercise.type == ExerciseType.STRENGTH)
            muscles.filter { it != muscleJoint }
        else
            joints.filter { it != muscleJoint }
        rvAddEditExerciseSecondaryMover.adapter = AddExerciseSecondaryRVAdapter(temp) {
                muscleJointAdapter, isSelected -> onSecondaryItemClick(muscleJointAdapter, isSelected)
        }
    }

    /**
     * Method sent to the secondary movers RecyclerView to handle the onItemCLick event. If the item was selected the secondary mover will be added
     * to the exercise. If the item was deselected the secondary mover will be removed from the exercise
     * @param muscleJoint Object containing the data on the selected/deselected secondary mover
     * @param isSelected flag denoting the state of the item clicked
     */
    private fun onSecondaryItemClick(muscleJoint: MuscleJoint, isSelected: Boolean) {
        if (isSelected)
            exercise.addSecondaryMover(muscleJoint)
        else
            exercise.removeSecondaryMover(muscleJoint)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        //nothing needs to happen here
    }
}
