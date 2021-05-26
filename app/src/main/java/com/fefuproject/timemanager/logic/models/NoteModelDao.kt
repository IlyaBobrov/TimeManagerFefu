package com.fefuproject.timemanager.logic.models

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface NoteModelDao {
    @Query("SELECT * FROM NoteModel")
    fun getAll(): List<NoteModel>

    @Query("SELECT * FROM NoteModel WHERE id IN (:noteIds)")
    fun loadAllByIds(noteIds: IntArray): List<NoteModel>

    @Query("SELECT * FROM NoteModel WHERE complete LIKE :complete LIMIT 1")
    fun findByComplete(complete: Boolean): NoteModel

    @Query("SELECT * FROM NoteModel WHERE date LIKE :date LIMIT 1")
    fun findByCDate(date: String): NoteModel

    @Insert
    fun insertAll(vararg notes: NoteModel)

    @Delete
    fun delete(note: NoteModel)
}