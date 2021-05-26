package com.fefuproject.timemanager.logic.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class CategoryModel(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String? = null,
) : Parcelable