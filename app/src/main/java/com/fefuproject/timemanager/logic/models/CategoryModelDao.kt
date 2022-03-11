package com.fefuproject.timemanager.logic.models

import androidx.room.*

@Dao
interface CategoryModelDao {
    @Query("SELECT * FROM CategoryModel")
    fun getAll(): List<CategoryModel>

    @Query("SELECT * FROM CategoryModel WHERE id IN (:Ids)")
    fun loadAllByIds(Ids: IntArray): List<CategoryModel>

    @Query("SELECT * FROM CategoryModel WHERE title LIKE :title LIMIT 1")
    fun findByTitle(title: String): CategoryModel

    @Insert
    fun insertAll(vararg categories: CategoryModel)

    @Update
    fun updateCategoryList(categoryModel: List<CategoryModel>)

    @Insert
    fun insertCategoryList(categoryModel: List<CategoryModel>)

    @Delete
    fun deleteCategoryList(categoryList: List<CategoryModel>)

    @Delete
    fun delete(note: CategoryModel)
}