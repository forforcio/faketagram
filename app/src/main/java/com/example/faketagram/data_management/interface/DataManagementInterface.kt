package com.example.faketagram.data_management.`interface`

import android.content.Context
import com.example.faketagram.data_management.model.User
import com.example.faketagram.data_management.service.ResourcesService

interface DataManagementInterface {

    context(resources: ResourcesService)
    fun getUsersFromJson(resId: Int)

    fun getAllUsers(): List<User>
}