package com.example.faketagram.data.service

import com.example.faketagram.data.`interface`.DataManagementInterface
import com.example.faketagram.data.model.User
import kotlinx.serialization.json.Json

class DataManagementService: DataManagementInterface {

    lateinit var users: List<User>

    context(resources: ResourcesService)
    override fun getUsersFromJson(resId: Int) {
        val jsonString = resources.getJsonTextById(resId)

        users = Json.decodeFromString<List<User>>(jsonString).map { user ->
            user.resId = resources.getResourceIdByImageName(user.resName)
            user.copy()
        }
    }

    override fun getAllUsers(): List<User> {
        return users
    }



}