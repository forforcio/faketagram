package com.example.faketagram.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class User (
    val userId: Int,
    val username: String,
    val age: Int,
    val bio: String,
    val resName: String,
    @Transient
    var resId: Int = 0,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (age != other.age) return false
        if (resId != other.resId) return false
        if (username != other.username) return false
        if (bio != other.bio) return false
        if (resName != other.resName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = age
        result = 31 * result + resId
        result = 31 * result + username.hashCode()
        result = 31 * result + bio.hashCode()
        result = 31 * result + resName.hashCode()
        return result
    }
}