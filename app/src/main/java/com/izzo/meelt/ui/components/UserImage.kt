package com.izzo.meelt.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.izzo.meelt.R
import com.izzo.meelt.data.model.User

@Composable
fun UserImage(
    user: User,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val assetPath = user.resAssetPath
    if (!assetPath.isNullOrBlank()) {
        AsyncImage(
            model = "file:///android_asset/$assetPath",
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
        return
    }

    Image(
        painter = painterResource(
            id = user.resId.takeIf { it != 0 } ?: R.drawable.default_user
        ),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

