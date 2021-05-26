package com.fefuproject.timemanager.logic.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fefuproject.timemanager.logic.models.NoteModel
import com.fefuproject.timemanager.logic.models.NoteModelDao

@Database(entities = [NoteModel::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteModelDao(): NoteModelDao
}