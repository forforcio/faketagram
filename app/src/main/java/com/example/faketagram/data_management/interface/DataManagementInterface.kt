package com.example.faketagram.data_management.`interface`

import android.content.Context
import com.example.faketagram.data_management.model.User

interface DataManagementInterface {

    fun initialize(context: Context)

    fun getUsersFromJson(resId: Int)

    fun getAllUsers(): List<User>
}