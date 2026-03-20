package com.example.faketagram.data.service

import android.content.Context

class ResourcesService(var context: Context) {

    fun getResourceIdByImageName(photoResName: String): Int {
        return context.resources.getIdentifier(
            photoResName,
            "drawable",
            context.packageName
        )
    }

    fun getJsonTextById(resId: Int): String {
        return context.resources.openRawResource(resId)
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
    }
}