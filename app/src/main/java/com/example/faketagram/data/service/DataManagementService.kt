package com.example.faketagram.data.service

import com.example.faketagram.data.model.User
import kotlinx.serialization.json.Json

class DataManagementService {

    lateinit var users: List<User>

    context(resources: ResourcesService)
    fun getUsersFromJson(resId: Int) {
        val jsonString = resources.getJsonTextById(resId)

        users = Json.decodeFromString<List<User>>(jsonString).map { user ->
            user.resId = resources.getResourceIdByImageName(user.resName)
            user.copy()
        }
    }

    fun getAllUsers(): List<User> {
        return users
    }



}