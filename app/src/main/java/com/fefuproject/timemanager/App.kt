package com.fefuproject.timemanager

import android.app.Application
import com.google.firebase.database.FirebaseDatabase


class App : Application() {

//    private lateinit var instance: App

//    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
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