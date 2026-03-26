package com.example.faketagram.data.service

import com.example.faketagram.data.model.User
import kotlinx.serialization.json.Json

class DataManagementService {

    lateinit var users: List<User>

    context(resources: ResourcesService)
    fun getUsersFromJson(resId: Int) {
        val jsonString = resources.getJsonTextById(resId)
        val json = Json {
            ignoreUnknownKeys = true
        }

        users = json.decodeFromString<List<User>>(jsonString).map { user ->
            user.copy(
                resId = resources.getResourceIdByImageNameOrDefault(user.resName),
                galleryResIds = resources.getResourceIdsByImageNames(user.galleryResName)
            )
        }
    }

    fun getAllUsers(): List<User> {
        return users
    }
}