package com.example.faketagram.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.faketagram.R

@Composable
fun StartProfileScreen(
    onLogoutButtonClicked: () -> Unit,
    modifier: Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Color.Transparent
                    )
                    .padding(top = 25.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.meelt_logo),
                    contentDescription = stringResource(R.string.content_desc_app_logo),
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.BottomCenter),
                )
            }
        }
    ) { innerPadding ->
        Button(
            onClick = { onLogoutButtonClicked() },
            modifier = Modifier.padding(innerPadding)
        ) {
            Text(text = stringResource(R.string.profile_logout))
        }
    }
}