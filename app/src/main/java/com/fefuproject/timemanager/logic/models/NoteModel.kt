package com.fefuproject.timemanager.logic.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class NoteModel(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String? = null,
    @ColumnInfo(name = "description") val description: String? = null,
    @ColumnInfo(name = "category") val category: String? = null,
    @ColumnInfo(name = "date_start") val dateStart: String? = null,
    @ColumnInfo(name = "date_end") val dateEnd: String? = null,
    @ColumnInfo(name = "complete") val complete: Boolean? = null
) : Parcelable
