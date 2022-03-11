package com.fefuproject.timemanager.logic.firebase.models

import java.util.*

data class CategoryFirebase(
    var id: String? = UUID.randomUUID().toString(),
    var title: String
)

