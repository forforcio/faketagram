package com.example.faketagram.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
                    .height(80.dp)
                    .background(
                        MaterialTheme.colorScheme.primary
                    )
                    .padding(20.dp)
            ) {
                Text(
                    text = "Marranos.com",
                    modifier = Modifier.align(Alignment.BottomStart),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    ) { innerPadding ->
        Button(
            onClick = { onLogoutButtonClicked() },
            modifier = Modifier.padding(innerPadding)
        ) {
            Text(text = "Logout")
        }
    }
}