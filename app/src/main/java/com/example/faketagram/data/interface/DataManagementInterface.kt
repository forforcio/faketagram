package com.example.faketagram.data.`interface`

import com.example.faketagram.data.model.User
import com.example.faketagram.data.service.ResourcesService

interface DataManagementInterface {

    context(resources: ResourcesService)
    fun getUsersFromJson(resId: Int)

    fun getAllUsers(): List<User>
}