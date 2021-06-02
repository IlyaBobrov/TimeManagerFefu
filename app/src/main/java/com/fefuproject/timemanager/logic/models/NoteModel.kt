package com.fefuproject.timemanager.logic.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class NoteModel(

    @PrimaryKey(autoGenerate = true)
    val id: Int? = 0,

    @ColumnInfo(name = "title")
    val title: String? = null,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "category")
    @TypeConverters(CategoryConverter::class)
    val category: CategoryModel? = null as CategoryModel,

    @ColumnInfo(name = "date_start")
    val dateStart: String? = null,

    @ColumnInfo(name = "date_end")
    val dateEnd: String? = null,

    @ColumnInfo(name = "complete")
    var complete: Boolean? = null

) : Parcelable
