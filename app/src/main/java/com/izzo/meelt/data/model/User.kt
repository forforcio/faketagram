package com.izzo.meelt.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class User (
    val userId: Int,
    val username: String = "",
    val firebaseUid: String = "",
    val age: Int = 0,
    val distance: Double = 0.0,
    val bio: String = "",
    val resName: String = "default_user",
    val galleryResName: List<String> = emptyList(),
    val isBlocked: Boolean = false,
    @Transient
    var resId: Int = 0,
    @Transient
    val galleryResIds: List<Int> = emptyList(),
    @Transient
    val resAssetPath: String? = null,
    @Transient
    val galleryResAssetPaths: List<String> = emptyList(),
) {
}