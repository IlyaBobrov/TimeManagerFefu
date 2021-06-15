package com.fefuproject.timemanager.logic.firebase.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize
import java.util.*

@IgnoreExtraProperties
@Parcelize
data class Items(
    var id: String? = UUID.randomUUID().toString(),
    var title: String,
    var text: String,
    var dateToDo: String,
    var deadline: String,
    var isComplited: Boolean,
    var category: String
) : Parcelable {


    @Exclude
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
    }
}
