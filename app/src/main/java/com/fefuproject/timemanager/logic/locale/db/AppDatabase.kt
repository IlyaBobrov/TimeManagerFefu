package com.fefuproject.timemanager.logic.locale.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fefuproject.timemanager.components.Constants.DATABASE_NAME
import com.fefuproject.timemanager.logic.locale.models.*

@Database(
    entities = [NoteModel::class, CategoryModel::class],
    version = 26
)
@TypeConverters(CategoryConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteModelDao(): NoteModelDao
    abstract fun categoryModelDao(): CategoryModelDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) =
            instance ?: synchronized(LOCK) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context,
            AppDatabase::class.java, DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }
}