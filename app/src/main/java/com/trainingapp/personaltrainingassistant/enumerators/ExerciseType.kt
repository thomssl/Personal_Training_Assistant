package com.trainingapp.personaltrainingassistant.enumerators

enum class ExerciseType(val value: Int, val text: String) {
    STRENGTH(1, "Strength"),
    MOBILITY(2, "Mobility"),
    STABILITY(3, "Stability"),
    BLANK(0, "Error")
}