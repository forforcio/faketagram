package com.example.faketagram.data_management.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class User (
    val username: String,
    val age: Int,
    val bio: String,
    val resName: String,
    @Transient
    var resId: Int = 0
)