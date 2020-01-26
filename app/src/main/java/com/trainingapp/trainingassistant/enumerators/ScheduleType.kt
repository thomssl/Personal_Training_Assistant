package com.trainingapp.trainingassistant.enumerators

enum class ScheduleType(val value: Int, val text: String) {
    WEEKLY_CONSTANT(1, "Weekly, Constant"),
    WEEKLY_VARIABLE(2, "Weekly, Variable"),
    MONTHLY_VARIABLE(3, "Monthly, Variable"),
    NO_SCHEDULE(0, "No Schedule"),
    BLANK(-1, "Empty")
}