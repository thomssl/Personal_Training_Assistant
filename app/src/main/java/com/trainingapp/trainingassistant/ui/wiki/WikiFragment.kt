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
        val result = loadWiki()//await loadWiki. true if no IOError, false if error occurred
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
     * Suspendable IO coroutine to load text from Wiki txt file. Finds the index for the line to start each section. Collects the text as needed for each section.
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
            //takes each sublist of lines and joins with new line character. Replaces tab and new line characters that have not been read properly with the proper notation
            inputFields = lines.subList(indexInputFields+1, indexInputFields+6).joinToString("\n").replace("\\t","\t").replace("\\n", "\n")
            clientsOverview = lines.subList(indexClientsOverview+1, indexClientsOverview + 10).joinToString("\n").replace("\\t","\t").replace("\\n", "\n")
            clientsClassification = lines.subList(indexClientsClassification+1, indexClientsClassification + 7).joinToString("\n").replace("\\t","\t").replace("\\n", "\n")
            clientsCreation = lines[indexClientsCreation+1].replace("\\n","\n")
            joints = lines.subList(indexJoints+1, indexJoints+11).joinToString("\n").replace("\\t","\t").replace("\\n", "\n")
            musclesOverview = lines.subList(indexMusclesOverview+1, indexMusclesOverview+4).joinToString("\n").replace("\\t","\t").replace("\\n", "\n")
            musclesCreation = lines[indexMusclesCreation+1].replace("\\n","\n")
            musclesDeletion = lines[indexMusclesDeletion+1].replace("\\n","\n")
            exercisesOverview = lines.subList(indexExercisesOverview+1, indexExercisesOverview+7).joinToString("\n").replace("\\t","\t").replace("\\n", "\n")
            exercisesClassification = lines.subList(indexExercisesClassification+1, indexExercisesClassification+9).joinToString("\n").replace("\\t","\t").replace("\\n", "\n")
            exercisesCreation = lines[indexExercisesCreation+1].replace("\\n","\n")
            exercisesDeletion = lines[indexExercisesDeletion+1].replace("\\n","\n")
            sessionsOverview = lines.subList(indexSessionsOverview+1, indexSessionsOverview+9).joinToString("\n").replace("\\t","\t").replace("\\n", "\n")
            sessionsLimits = lines.subList(indexSessionsLimits+1, indexSessionsLimits+4).joinToString("\n").replace("\\t","\t").replace("\\n", "\n")
            true
        } catch (e: Exception){
            e.printStackTrace()
            false
        }
    }
}