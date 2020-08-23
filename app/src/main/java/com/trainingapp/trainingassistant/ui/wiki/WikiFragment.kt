package com.trainingapp.trainingassistant.ui.wiki

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.trainingapp.trainingassistant.R
import kotlinx.android.synthetic.main.fragment_wiki.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Fragment called upon from 'Wiki' action within NavigationDrawer. Used to display wiki or README to user
 */
class WikiFragment : Fragment(), CoroutineScope  {

    private var introduction = "Error. Did not read Wiki properly. Whoops"
    private var inputFields = "Error. Did not read Wiki properly. Whoops"
    private var clientsOverview = "Error. Did not read Wiki properly. Whoops"
    private var clientsClassification = "Error. Did not read Wiki properly. Whoops"
    private var clientsCreation = "Error. Did not read Wiki properly. Whoops"
    private var joints = "Error. Did not read Wiki properly. Whoops"
    private var musclesOverview = "Error. Did not read Wiki properly. Whoops"
    private var musclesCreation = "Error. Did not read Wiki properly. Whoops"
    private var musclesDeletion = "Error. Did not read Wiki properly. Whoops"
    private var exercisesOverview = "Error. Did not read Wiki properly. Whoops"
    private var exercisesClassification = "Error. Did not read Wiki properly. Whoops"
    private var exercisesCreation = "Error. Did not read Wiki properly. Whoops"
    private var exercisesDeletion = "Error. Did not read Wiki properly. Whoops"
    private var sessionsOverview = "Error. Did not read Wiki properly. Whoops"
    private var sessionsLimits = "Error. Did not read Wiki properly. Whoops"
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wiki, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateView(view)
    }

    /**
     * UI coroutine used to fill scroll view fields with the proper text for the wiki
     */
    private fun populateView(view: View) = launch {
        // Await loadWiki. true if no IOError, false if error occurred
        val result = loadWiki()
        prgWikiData.visibility = View.INVISIBLE
        if (result){
            wikiIntroductionBody.text = introduction
            wikiInputsFieldsBody.text = inputFields
            wikiClientsOverviewBody.text = clientsOverview
            wikiClientsClassificationBody.text = clientsClassification
            wikiClientsCreationBody.text = clientsCreation
            wikiJointsBody.text = joints
            wikiMusclesOverviewBody.text = musclesOverview
            wikiMusclesCreationBody.text = musclesCreation
            wikiMusclesDeletionBody.text = musclesDeletion
            wikiExercisesOverviewBody.text = exercisesOverview
            wikiExercisesClassificationBody.text = exercisesClassification
            wikiExercisesCreationBody.text = exercisesCreation
            wikiExercisesDeletionBody.text = exercisesDeletion
            wikiSessionsOverviewBody.text = sessionsOverview
            wikiSessionsLimitsBody.text = sessionsLimits
            svWiki.visibility = View.VISIBLE
        } else
            Snackbar.make(view, "Error loading Wiki", Snackbar.LENGTH_LONG).show()
    }

    /**
     * Suspendable IO coroutine to load text from Wiki txt file. Finds the index for the line to start each section. Collects the text as needed for
     * each section.
     * Hard coded for now. Might change to accept any wiki txt file formatted to specific conditions
     * @return true if no error occurs, false if error occurs
     */
    private suspend fun loadWiki(): Boolean = withContext(Dispatchers.IO){
        try {
            val input = context!!.assets.open("PTAPPWiki2.txt")
            val reader = input.bufferedReader()
            val lines = reader.readLines()
            val indexInputFields = lines.indexOf("Input Fields")
            val indexClientsOverview = lines.indexOf("Client Overview")
            val indexClientsClassification = lines.indexOf("Client Classification")
            val indexClientsCreation = lines.indexOf("Client Creation")
            val indexJoints = lines.indexOf("Joint")
            val indexMusclesOverview = lines.indexOf("Muscle Overview")
            val indexMusclesCreation = lines.indexOf("Muscle Deletion")
            val indexMusclesDeletion = lines.indexOf("Muscle Deletion")
            val indexExercisesOverview = lines.indexOf("Exercise Overview")
            val indexExercisesClassification = lines.indexOf("Exercise Classification")
            val indexExercisesCreation = lines.indexOf("Exercise Creation")
            val indexExercisesDeletion = lines.indexOf("Exercise Deletion")
            val indexSessionsOverview = lines.indexOf("Session Overview")
            val indexSessionsLimits = lines.indexOf("Session Limits")
            introduction = lines[1]
            // Takes each sublist of lines and joins with new line character. Replaces tab and new line characters with escaped \ to the white space
            // character
            inputFields = lines
                .sliceFor(indexInputFields + 1, 6)
                .joinToString("\n") { removeEscapes(it) }
            clientsOverview = lines
                .sliceFor(indexClientsOverview + 1, 9)
                .joinToString("\n") { removeEscapes(it) }
            clientsClassification = lines
                .sliceFor(indexClientsClassification + 1, 6)
                .joinToString("\n") { removeEscapes(it) }
            clientsCreation = lines[indexClientsCreation+1].replace("\\n","\n")
            joints = lines
                .sliceFor(indexJoints + 1, 10)
                .joinToString("\n") { removeEscapes(it) }
            musclesOverview = lines
                .sliceFor(indexMusclesOverview + 1, 3)
                .joinToString("\n") { removeEscapes(it) }
            musclesCreation = removeEscapes(lines[indexMusclesCreation + 1])
            musclesDeletion = removeEscapes(lines[indexMusclesDeletion + 1])
            exercisesOverview = lines
                .sliceFor(indexExercisesOverview + 1, 6)
                .joinToString("\n") { removeEscapes(it) }
            exercisesClassification = lines
                .sliceFor(indexExercisesClassification + 1, 8)
                .joinToString("\n") { removeEscapes(it) }
            exercisesCreation = removeEscapes(lines[indexExercisesCreation + 1])
            exercisesDeletion = removeEscapes(lines[indexExercisesDeletion + 1])
            sessionsOverview = lines
                .sliceFor(indexSessionsOverview + 1, 8)
                .joinToString("\n") { removeEscapes(it) }
            sessionsLimits = lines
                .sliceFor(indexSessionsLimits + 1, 3)
                .joinToString("\n") { removeEscapes(it) }
            true
        } catch (e: Exception){
            e.printStackTrace()
            false
        }
    }

    private fun removeEscapes(str: String) = str.replace("\\t","\t").replace("\\n", "\n")

    private fun List<String>.sliceFor(start: Int, length: Int) = this.subList(start, start + length)
}