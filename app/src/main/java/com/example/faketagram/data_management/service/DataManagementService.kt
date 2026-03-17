package com.example.faketagram.data_management.service

import android.content.Context
import com.example.faketagram.data_management.`interface`.DataManagementInterface
import com.example.faketagram.data_management.model.User
import kotlinx.serialization.json.Json

class DataManagementService: DataManagementInterface {

    lateinit var context: Context
    lateinit var users: List<User>

    override fun initialize(context: Context) {
        this.context = context
    }

    override fun getUsersFromJson(resId: Int) {
        val jsonString = context.resources.openRawResource(resId)
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }

        users = Json.decodeFromString<List<User>>(jsonString).map { user ->
            user.resId = getResourceIdByImageName(user.resName)
            user.copy()
        }
    }

    override fun getAllUsers(): List<User> {
        return users
    }

    fun getResourceIdByImageName(photoResName: String): Int {
        return context.resources.getIdentifier(
            photoResName,
            "drawable",
            context.packageName
        )
    }

}