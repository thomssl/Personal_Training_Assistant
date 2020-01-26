package com.trainingapp.trainingassistant.enumerators

enum class ExerciseType(val num: Int, val text: String) {
    STRENGTH(1, "Strength"),
    MOBILITY(2, "Mobility"),
    STABILITY(3, "Stability"),
    BLANK(4, "Error")
}