package com.example.faketagram.data.model

data class Message(
    val text: String? = null,
    val photoUrl: String? = null,
    val imageUrl: String? = null,
    val senderUid: String? = null,
    val receiverUid: String? = null,
    val timestamp: Long = 0L,
)