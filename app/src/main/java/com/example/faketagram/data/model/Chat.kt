package com.example.faketagram.data.model

import kotlinx.serialization.Serializable


data class Chat(
    var userId: Int,
    var messages: List<Message> = emptyList()) {
}