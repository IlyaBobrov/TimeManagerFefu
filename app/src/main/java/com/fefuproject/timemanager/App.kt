package com.fefuproject.timemanager

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.fefuproject.timemanager.logic.db.AppDatabase


class App : Application() {

//    private lateinit var instance: App

//    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
//        instance = this
        /*database = Room.databaseBuilder(this, AppDatabase::class.java, "database-note")
            .build()*/
    }

    /*fun getInstance(): App {
        return instance
    }

    fun getDatabase(): AppDatabase {
        return database
    }*/
}