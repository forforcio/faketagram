package com.izzo.meelt.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppDataset(
    val users: List<User> = emptyList(),
    val messages: List<Message> = emptyList(),
)

