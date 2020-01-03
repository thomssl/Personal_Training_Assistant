package com.trainingapp.personaltrainingassistant.database

import android.provider.BaseColumns

object DBInfo {

    class ClientsEntry : BaseColumns{
        companion object {
            const val ID = "client_id"
            const val NAME = "client_name"
            const val SCHEDULE_TYPE = "schedule_type"
            const val DAYS = "days"
            const val TIMES = "times"
            const val DURATIONS = "durations"
            const val START_DATE = "start_date"
            const val END_DATE = "end_date"
        }
    }

    class ExerciseTypesEntry : BaseColumns{
        companion object {
            const val ID = "exercise_type_id"
            const val NAME = "exercise_type_name"
        }
    }

    class ExercisesEntry : BaseColumns{
        companion object {
            const val ID = "exercise_id"
            const val NAME = "exercise_name"
            const val TYPE = "exercise_type"
            const val PRIMARY_MOVER = "primary_mover"
            const val SECONDARY_MOVERS = "secondary_movers"
        }
    }

    class JointsEntry : BaseColumns{
        companion object {
            const val ID = "joint_id"
            const val NAME = "joint_name"
        }
    }

    class MusclesEntry : BaseColumns{
        companion object {
            const val ID = "muscle_id"
            const val NAME = "muscle_name"
        }
    }

    class SessionChangesEntry : BaseColumns{
        companion object {
            const val CLIENT_ID = "client_id"
            const val NORMAL_DAYTIME = "normal_dayTime"
            const val CHANGE_DAYTIME = "change_dayTime"
            const val DURATION = "duration"
        }
    }

    class SessionLogEntry : BaseColumns{
        companion object{
            const val CLIENT_ID = "client_id"
            const val DAYTIME = "dayTime"
            const val EXERCISE_IDS = "exercise_ids"
            const val SETS = "sets"
            const val REPS = "reps"
            const val RESISTANCE = "resistances"
            const val EXERCISE_ORDER = "exercise_order"
            const val NOTES = "notes"
            const val DURATION = "duration"
        }
    }

    class UserSettingsEntry : BaseColumns{
        companion object{
            const val DEFAULT_DURATION = "default_duration"
            const val CLOCK_24 = "'24_clock'"
        }
    }
}