package com.trainingapp.personaltrainingassistant.ui.wiki

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.trainingapp.personaltrainingassistant.R
import kotlinx.android.synthetic.main.fragment_wiki.*

class WikiFragment : Fragment()  {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wiki, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadWiki()
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
    }

    private fun loadWiki(){
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
            inputFields = lines.subList(indexInputFields+1, indexInputFields + 6).joinToString("\n").replace("\\t","\t").replace("\\n", "\n")
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
            //inputFields = "${lines[indexInputFields+1]}\n\t\t${lines[indexInputFields+2]}\n\t\t${lines[indexInputFields+3]}\n\t\t${lines[indexInputFields+4]}\n\t\t${lines[indexInputFields+5]}"
            //sessions = lines.subList(indexSessions, indexSessions + 4).joinToString("\n\n")
            //sessions = "${lines[indexSessions+1]}\n\n${lines[indexSessions+2]}\n\n${lines[indexSessions+3]}"
            //exercises = lines.subList(indexExercises, lines.lastIndex).joinToString("\n")
            //exercises = exercises.replace("\\t", "\t").replace("\\n", "\n")
            //exercises = "${lines[indexExercises+1]}\n\n\t\t${lines[indexExercises+2]}\n\t\t${lines[indexExercises+3]}\n\t\t${lines[indexExercises+4]}\n\n${lines[indexExercises+5]}\n\n\t\t${lines[indexExercises+6]}\n\t\t${lines[indexExercises+7]}\n\n${lines[indexExercises+8]}"
        } catch (e: Exception){
            e.printStackTrace()
        }
    }
}