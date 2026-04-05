package com.izzo.meelt.data.service

import android.content.Context
import androidx.core.content.edit
import com.izzo.meelt.data.model.AppDataset
import com.izzo.meelt.data.model.Message
import com.izzo.meelt.data.model.User
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class DataManagementService {

    lateinit var users: List<User>
    var messages: List<Message> = emptyList()
    var usersResourceId: Int = 0

    fun getAvailableUsersJsonNames(appContext: Context): List<String> {
        return appContext.assets
            .list(FEEDS_ASSETS_DIR)
            .orEmpty()
            .filter { feedName ->
                appContext.assets
                    .list("$FEEDS_ASSETS_DIR/$feedName")
                    .orEmpty()
                    .contains(USERS_JSON_FILE_NAME)
            }
            .sorted()
    }

    fun getSelectedUsersJsonName(appContext: Context): String {
        val availableJsonNames = getAvailableUsersJsonNames(appContext)
        if (availableJsonNames.isEmpty()) return ""

        val savedJsonName = getPreferences(appContext)
            .getString(PREFERENCE_SELECTED_USERS_JSON_NAME, null)

        return savedJsonName
            ?.takeIf { it in availableJsonNames }
            ?: availableJsonNames.first()
    }

    context(resources: ResourcesService)
    fun loadSelectedUsersFromPreferences(appContext: Context): String {
        val selectedJsonName = getSelectedUsersJsonName(appContext)
        if (selectedJsonName.isBlank()) {
            users = emptyList()
            messages = emptyList()
            usersResourceId = 0
            return ""
        }
        loadUsersByJsonName(selectedJsonName)
        return selectedJsonName
    }

    context(resources: ResourcesService)
    fun selectUsersJson(appContext: Context, jsonName: String): String {
        val availableJsonNames = getAvailableUsersJsonNames(appContext)
        if (availableJsonNames.isEmpty()) {
            users = emptyList()
            messages = emptyList()
            usersResourceId = 0
            getPreferences(appContext)
                .edit()
                .remove(PREFERENCE_SELECTED_USERS_JSON_NAME)
                .apply()
            return ""
        }

        val selectedJsonName = jsonName
            .takeIf { it in availableJsonNames }
            ?: availableJsonNames.first()

        getPreferences(appContext).edit {
            putString(PREFERENCE_SELECTED_USERS_JSON_NAME, selectedJsonName)
        }

        loadUsersByJsonName(selectedJsonName)
        return selectedJsonName
    }

    context(resources: ResourcesService)
    fun getUsersFromJson(resId: Int) {
        usersResourceId = resId
        val jsonString = resources.getJsonTextById(resId)
        applyDatasetFromJson(jsonString)
    }

    fun getAllUsers(): List<User> {
        return users
    }

    fun getAllMessages(): List<Message> {
        return messages
    }

    context(resources: ResourcesService)
    private fun loadUsersByJsonName(jsonName: String) {
        val assetPath = "$FEEDS_ASSETS_DIR/$jsonName/$USERS_JSON_FILE_NAME"
        val jsonFromAssets = runCatching {
            resources.context.assets.open(assetPath)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
        }.getOrNull()

        if (jsonFromAssets != null) {
            usersResourceId = 0
            applyDatasetFromJson(jsonFromAssets)
            return
        }

        users = emptyList()
        messages = emptyList()
        usersResourceId = 0
    }

    context(resources: ResourcesService)
    private fun applyDatasetFromJson(jsonString: String) {
        val json = Json {
            ignoreUnknownKeys = true
        }

        val dataset = decodeDataset(json, jsonString)

        users = dataset.users.map { user ->
            val profileResId = resources.getResourceIdByImageName(user.resName)
            val profileAssetPath = user.resName.takeIf {
                profileResId == 0 && resources.assetExists(it)
            }

            val galleryResIds = resources.getResourceIdsByImageNames(user.galleryResName)
            val galleryAssetPaths = user.galleryResName.filter { imagePath ->
                resources.getResourceIdByImageName(imagePath) == 0 && resources.assetExists(imagePath)
            }

            user.copy(
                resId = profileResId.takeIf { it != 0 }
                    ?: resources.getResourceIdByImageName("default_user"),
                galleryResIds = galleryResIds,
                resAssetPath = profileAssetPath,
                galleryResAssetPaths = galleryAssetPaths
            )
        }
        messages = dataset.messages.sortedBy { it.timestamp }
    }

    private fun getPreferences(appContext: Context) =
        appContext.applicationContext.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    private fun decodeDataset(json: Json, jsonString: String): AppDataset {
        return runCatching {
            json.decodeFromString<AppDataset>(jsonString)
        }.getOrElse {
            AppDataset(
                users = json.decodeFromString(ListSerializer(User.serializer()), jsonString),
                messages = emptyList()
            )
        }
    }

    private companion object {
        private const val PREFERENCES_NAME = "data_management_preferences"
        private const val PREFERENCE_SELECTED_USERS_JSON_NAME = "selected_users_json_name"
        private const val FEEDS_ASSETS_DIR = "feeds"
        private const val USERS_JSON_FILE_NAME = "users.json"
    }
}