package com.fefuproject.timemanager.logic.firebase.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class NoteFirebase(
    var id: String? = UUID.randomUUID().toString(),
    var title: String,
    var text: String,
    var dateToDo: String,
    var deadline: String,
    @field:JvmField
    var isComplited: Boolean,
    var category: String
) : Parcelable {


/*    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "text" to text,
            "dateToDo" to dateToDo,
            "deadline" to deadline,
            "isComplited" to isComplited,
            "category" to category
        )
    }*/
}