package com.trainingapp.personaltrainingassistant.database

import android.provider.BaseColumns

object DBInfo2 {

    class ClientsTable : BaseColumns{
        companion object {
            const val ID = "client_id"
            const val NAME = "client_name"
            const val START_DATE = "start_date"
            const val END_DATE = "end_date"
        }
    }

    class ExercisesTable : BaseColumns{
        companion object {
            const val ID = "exercise_id"
            const val NAME = "exercise_name"
            const val TYPE = "exercise_type"
            const val PRIMARY_MOVER = "primary_mover"
        }
    }

    class JointsTable : BaseColumns{
        companion object {
            const val ID = "joint_id"
            const val NAME = "joint_name"
        }
    }

    class MusclesTable : BaseColumns{
        companion object {
            const val ID = "muscle_id"
            const val NAME = "muscle_name"
        }
    }

    class ProgramExercisesTable : BaseColumns{
        companion object {
            const val PROGRAM_ID = "program_id"
            const val EXERCISE_ID = "exercise_id"
            const val SETS = "sets"
            const val REPS = "reps"
            const val RESISTANCE = "resistance"
            const val EXERCISE_ORDER = "exercise_order"
        }
    }

    class ProgramsTable : BaseColumns{
        companion object {
            const val PROGRAM_ID = "program_id"
            const val CLIENT_ID = "client_id"
            const val DAYTIME = "dayTime"
            const val NAME = "name"
        }
    }

    class SchedulesTable : BaseColumns{
        companion object {
            const val CLIENT_ID = "client_id"
            const val SCHEDULE_TYPE = "schedule_type"
            const val DAYS = "days"
            const val DURATION = "duration"
            const val SUN = "sun"
            const val MON = "mon"
            const val TUE = "tue"
            const val WED = "wed"
            const val THU = "thu"
            const val FRI = "fri"
            const val SAT = "sat"
            const val SUN_DURATION = "sun_duration"
            const val MON_DURATION = "mon_duration"
            const val TUE_DURATION = "tue_duration"
            const val WED_DURATION = "wed_duration"
            const val THU_DURATION = "thu_duration"
            const val FRI_DURATION = "fri_duration"
            const val SAT_DURATION = "sat_duration"
        }
    }

    class SecondaryMoversTable : BaseColumns{
        companion object {
            const val EXERCISE_ID = "exercise_id"
            const val SECONDARY_MOVER_ID = "secondary_mover_id"
        }
    }

    class SessionChangesTable : BaseColumns{
        companion object {
            const val CLIENT_ID = "client_id"
            const val NORMAL_DAYTIME = "normal_dayTime"
            const val CHANGE_DAYTIME = "change_dayTime"
            const val DURATION = "duration"
        }
    }

    class SessionLogTable : BaseColumns{
        companion object{
            const val CLIENT_ID = "client_id"
            const val DAYTIME = "dayTime"
            const val PROGRAM_ID = "program_id"
            const val EXERCISE_ORDER = "exercise_order"
            const val NOTES = "notes"
            const val DURATION = "duration"
        }
    }

    class UserSettingsTable : BaseColumns{
        companion object{
            const val DEFAULT_DURATION = "default_duration"
            const val CLOCK_24 = "'24_clock'"
        }
    }
}