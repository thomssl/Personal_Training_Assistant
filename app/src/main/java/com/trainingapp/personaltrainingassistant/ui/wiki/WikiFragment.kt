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
    private var sessions = "Error. Did not read Wiki properly. Whoops"
    private var exercises = "Error. Did not read Wiki properly. Whoops"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wiki, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadWiki()
        wikiIntroductionBody.text = introduction
        wikiInputsFieldsBody.text = inputFields
        wikiSessionsBody.text = sessions
        wikiExerciseBody.text = exercises
    }

    private fun loadWiki(){
        try {
            val input = context!!.assets.open("PTAppWiki.txt")
            val reader = input.bufferedReader()
            val lines = reader.readLines()
            val indexInputFields = lines.indexOf("Input Fields")
            val indexSessions = lines.indexOf("Sessions")
            val indexExercises = lines.indexOf("Exercises")
            introduction = lines[1]
            inputFields = "${lines[indexInputFields+1]}\n\t\t${lines[indexInputFields+2]}\n\t\t${lines[indexInputFields+3]}\n\t\t${lines[indexInputFields+4]}\n\t\t${lines[indexInputFields+5]}"
            sessions = "${lines[indexSessions+1]}\n\n${lines[indexSessions+2]}\n\n${lines[indexSessions+3]}"
            exercises = "${lines[indexExercises+1]}\n\n\t\t${lines[indexExercises+2]}\n\t\t${lines[indexExercises+3]}\n\t\t${lines[indexExercises+4]}\n\n${lines[indexExercises+5]}\n\n\t\t${lines[indexExercises+6]}\n\t\t${lines[indexExercises+7]}\n\n${lines[indexExercises+8]}"
        } catch (e: Exception){
            e.printStackTrace()
        }
    }
}