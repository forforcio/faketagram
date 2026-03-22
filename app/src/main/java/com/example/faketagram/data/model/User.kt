package com.example.faketagram.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class User (
    val userId: Int,
    val username: String = "default user",
    val firebaseUid: String = "zero",
    val age: Int = 0,
    val bio: String = "this is default user's bio",
    val resName: String = "wenaso_1",
    @Transient
    var resId: Int = 0,
) {
}