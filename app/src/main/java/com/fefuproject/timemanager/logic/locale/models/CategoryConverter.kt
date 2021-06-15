package com.fefuproject.timemanager.logic.locale.models

import androidx.room.TypeConverter


class CategoryConverter {
    @TypeConverter
    fun fromCategory(category: CategoryModel): String? {
        return category.title
    }

    @TypeConverter
    fun toCategory(data: String): CategoryModel {
        return CategoryModel(null ,data)
    }
}