package com.fefuproject.timemanager.logic.locale.models

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CategoryModelDao {
    @Query("SELECT * FROM CategoryModel")
    fun getAll(): List<CategoryModel>

    @Query("SELECT * FROM CategoryModel WHERE id IN (:Ids)")
    fun loadAllByIds(Ids: IntArray): List<CategoryModel>

    @Query("SELECT * FROM CategoryModel WHERE title LIKE :title LIMIT 1")
    fun findByTitle(title: String): CategoryModel

    @Insert
    fun insertAll(vararg notes: CategoryModel)

    @Delete
    fun delete(note: CategoryModel)
}