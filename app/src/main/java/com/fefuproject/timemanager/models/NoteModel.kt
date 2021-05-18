package com.fefuproject.timemanager.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class NoteListModel(
    internal val data: List<NoteModel>
)

@Parcelize
data class NoteModel(
    internal val id: Int,
    internal val category: String,
    internal val date: String,
    internal val message: String,
    internal val complete: Boolean
) : Parcelable