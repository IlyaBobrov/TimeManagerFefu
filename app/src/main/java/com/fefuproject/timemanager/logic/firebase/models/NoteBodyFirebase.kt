package com.fefuproject.timemanager.logic.firebase.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

data class NoteBodyFirebase(
    var title: String,
    var text: String,
    var dateToDo: String,
    var deadline: String,
    var isComplited: Boolean,
    var category: String
)