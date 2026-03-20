package com.example.faketagram.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    var content: String,
    var sender: User? = null,
    var receiver: User? = null,
    var read: Boolean = false,
    var sent: Boolean = false) {

}