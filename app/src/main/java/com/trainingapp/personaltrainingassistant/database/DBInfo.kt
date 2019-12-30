package com.trainingapp.personaltrainingassistant.database

import android.provider.BaseColumns

object DBInfo {

    class ClientsEntry : BaseColumns{
        companion object {
            val ID = "id"
            val NAME = "name"
            val SESSION_TYPE = "session_type"
            val DAYS = "days"
            val TIMES = "times"
            val DURATIONS = "durations"
            val START_DATE = "start_date"
            val END_DATE = "end_date"
        }
    }

    class ExerciseTypesEntry : BaseColumns{
        companion object {
            val ID = "id"
            val NAME = "name"
        }
    }

    class ExercisesEntry : BaseColumns{
        companion object {
            val ID = "id"
            val NAME = "name"
            val TYPE = "type"
            val PRIMARY_MOVER = "primary_mover"
            val SECONDARY_MOVERS = "secondary_movers"
        }
    }

    class JointsEntry : BaseColumns{
        companion object {
            val ID = "id"
            val NAME = "name"
        }
    }

    class MuslcesEntry : BaseColumns{
        companion object {
            val ID = "id"
            val NAME = "name"
        }
    }

    class SessionChangesEntry : BaseColumns{
        companion object {
            val CLIENT_ID = "client_id"
            val NORMAL_DAYTIME = "normal_dayTime"
            val CHANGE_DAYTIME = "change_dayTime"
            val DURATION = "duration"
        }
    }

    class SessionLogEntry : BaseColumns{
        companion object{
            val CLIENT_ID = "client_id"
            val DAYTIME = "dayTime"
            val EXERCISE_IDS = "exercise_ids"
            val SETS = "sets"
            val REPS = "reps"
            val RESISTANCE = "resistance"
            val EXERCISE_ORDER = "exercise_order"
            val NOTES = "notes"
            val DURATION = "duration"
        }
    }

    class UserSettingsEntry : BaseColumns{
        companion object{
            val DEFAULT_DURATION = "default_duration"
            val CLOCK_24 = "24_clock"
        }
    }
}