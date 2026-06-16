package com.izzo.meelt.data.service

import android.content.Context
import java.io.IOException

class ResourcesService(var context: Context) {

    fun getResourceIdByImageName(photoResName: String): Int {
        return context.resources.getIdentifier(
            photoResName,
            "drawable",
            context.packageName
        )
    }

    fun getResourceIdByImageNameOrDefault(
        photoResName: String,
    ): Int {
        return getResourceIdByImageName(photoResName)
            .takeIf { it != 0 }
            ?: getResourceIdByImageName("default_user")
    }

    fun getResourceIdsByImageNames(photoResNames: List<String>): List<Int> {
        return photoResNames.mapNotNull { name ->
            getResourceIdByImageName(name).takeIf { it != 0 }
        }
    }

    fun getJsonTextById(resId: Int): String {
        return context.resources.openRawResource(resId)
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
    }

    fun getJsonTextFromAssetOrNull(assetPath: String): String? {
        return try {
            context.assets.open(assetPath)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
        } catch (_: IOException) {
            null
        }
    }

    fun assetExists(assetPath: String): Boolean {
        return try {
            context.assets.open(assetPath).use { }
            true
        } catch (_: IOException) {
            false
        }
    }

    fun toAssetUri(assetPath: String): String = "file:///android_asset/$assetPath"
}