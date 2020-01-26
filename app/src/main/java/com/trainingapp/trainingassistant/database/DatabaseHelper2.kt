package com.trainingapp.trainingassistant.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper2(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    /**
     * Need to update these table definitions
     */
    companion object {
        private const val DATABASE_NAME: String = "data.sqlite3"
        private const val DATABASE_VERSION: Int = 1
        private const val CREATE1 = "Create Table If Not Exists Clients(client_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, client_name TEXT NOT NULL UNIQUE, schedule_type INTEGER NOT NULL, days TEXT NOT NULL, times TEXT NOT NULL, durations TEXT NOT NULL, start_date TEXT NOT NULL, end_date TEXT NOT NULL)"
        private const val CREATE2 = "Create Table If Not Exists Exercise_Types(exercise_type_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, exercise_type_name TEXT NOT NULL UNIQUE)"
        private const val CREATE3 = "Create Table If Not Exists Exercises(exercise_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, exercise_name TEXT NOT NULL UNIQUE, exercise_type INTEGER NOT NULL, primary_movers INTEGER NOT NULL, secondary_movers TEXT NOT NULL)"
        private const val CREATE4 = "Create Table If Not Exists Muscles(muscle_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, muscle_name TEXT NOT NULL UNIQUE)"
        private const val CREATE5 = "Create Table If Not Exists Session_Changes(client_id INTEGER NOT NULL, normal_dayTime TEXT NOT NULL, change_dayTime TEXT NOT NULL, duration INTEGER NOT NULL)"
        private const val CREATE6 = "Create Table If Not Exists Session_log(client_id INTEGER NOT NULL, dayTime TEXT NOT NULL, exercise_ids TEXT NOT NULL, sets TEXT NOT NULL, reps TEXT NOT NULL, resistances TEXT NOT NULL, exercise_order TEXT NOT NULL, notes TEXT NOT NULL, duration TEXT NOT NULL)"
        private const val CREATE7 = "Create Table If Not Exists Variable_Sessions(client_id INTEGER NOT NULL, dayTime TEXT NOT NULL)"
        private const val CREATE8 = "Create Table If Not Exists Joints(joint_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, joint_name TEXT NOT NULL)"
        private const val CREATE9 = "Create Table If Not Exists User_Settings(default_duration INTEGER NOT NULL, '24_clock' INTEGER NOT NULL)"
    }

    override fun onCreate(db: SQLiteDatabase) {
        /*db.execSQL(CREATE1)
        db.execSQL(CREATE2)
        db.execSQL(CREATE3)
        db.execSQL(CREATE4)
        db.execSQL(CREATE5)
        db.execSQL(CREATE6)
        db.execSQL(CREATE7)
        db.execSQL(CREATE8)
        db.execSQL(CREATE9)*/
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        //onCreate(db)
    }
}