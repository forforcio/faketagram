package com.example.faketagram.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val messageId: String? = null,
    val text: String? = null,
    val photoUrl: String? = null,
    val imageUrl: String? = null,
    val senderUid: String? = null,
    val receiverUid: String? = null,
    val timestamp: Long = 0L,
    val read: Boolean = false,
)