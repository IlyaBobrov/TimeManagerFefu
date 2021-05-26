package com.fefuproject.timemanager.logic.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

data class NoteListModel(
    internal val data: List<NoteModel>
)

@Parcelize
@Entity
data class NoteModel(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "category") val category: String? = null,
    @ColumnInfo(name = "date") val date: String? = null,
    @ColumnInfo(name = "message") val message: String? = null,
    @ColumnInfo(name = "complete") val complete: Boolean? = null
) : Parcelable