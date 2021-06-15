package com.fefuproject.timemanager.logic.firebase.models

import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

@IgnoreExtraProperties
data class Categories(
    var id: String? = UUID.randomUUID().toString(),
    var title: String
)