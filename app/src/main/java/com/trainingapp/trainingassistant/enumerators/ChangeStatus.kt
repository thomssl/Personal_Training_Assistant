package com.trainingapp.trainingassistant.enumerators

enum class ChangeStatus(val color: Int, val rgb: String) {
    UNCONFIRMED (0xE3AE0E, "#E3AE0E"),
    CONFIRMED (0x488F38, "#488F38"),
    ERROR (0xAE1526, "#AE1526"),
    NOTHING (0x808080, "#808080")
}