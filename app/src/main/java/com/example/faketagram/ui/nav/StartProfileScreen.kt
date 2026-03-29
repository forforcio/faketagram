package com.example.faketagram.ui.nav

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.faketagram.R

@Composable
fun StartProfileScreen(
    availableUsersJsonNames: List<String>,
    selectedUsersJsonName: String,
    onUsersJsonSelected: (String) -> Unit,
    onLogoutButtonClicked: () -> Unit,
    modifier: Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val selectedUsersJsonLabel = when {
        selectedUsersJsonName.isNotBlank() -> "${selectedUsersJsonName}.json"
        availableUsersJsonNames.isNotEmpty() -> "${availableUsersJsonNames.first()}.json"
        else -> stringResource(R.string.profile_users_json_none)
    }

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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
            ) {
            Button(
                onClick = { onLogoutButtonClicked() },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
            ) {
                Text(text = stringResource(R.string.profile_logout))
            }

            Spacer(modifier = Modifier.size(12.dp))

            Box {
                Button(
                    onClick = { menuExpanded = true },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                        text = stringResource(
                            R.string.profile_users_json_selector,
                            selectedUsersJsonLabel
                        )
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    availableUsersJsonNames.forEach { jsonName ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (jsonName == selectedUsersJsonName) {
                                        "✓ ${jsonName}.json"
                                    } else {
                                        "${jsonName}.json"
                                    }
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onUsersJsonSelected(jsonName)
                            }
                        )
                    }
                }
            }
        }
    }
}