package com.fefuproject.timemanager.logic.models

import androidx.room.*


@Dao
interface NoteModelDao {
    @Query("SELECT * FROM NoteModel")
    fun getAll(): List<NoteModel>

    @Query("SELECT * FROM NoteModel WHERE id IN (:noteIds)")
    fun loadAllByIds(noteIds: IntArray): List<NoteModel>

    @Query("SELECT * FROM NoteModel WHERE complete LIKE :complete ")
    fun findByNotCompleted(complete: Boolean): List<NoteModel>

    @Query("SELECT * FROM NoteModel WHERE date_start LIKE :date")
    fun findByDate(date: String): List<NoteModel>

    @Query("SELECT * FROM NoteModel WHERE category LIKE :category")
    fun findByCategory(category: String): List<NoteModel>

    @Update
    fun updateNote(note: NoteModel)

    @Update
    fun updateNoteList(noteList: List<NoteModel>)

    @Insert
    fun insertNoteList(noteList: List<NoteModel>)

    @Insert
    fun insertAll(vararg notes: NoteModel)

    @Delete
    fun deleteNote(note: NoteModel)

    @Delete
    fun deleteNoteList(noteList: List<NoteModel>)

    @Query("DELETE FROM NoteModel")
    fun deleteAll()
}