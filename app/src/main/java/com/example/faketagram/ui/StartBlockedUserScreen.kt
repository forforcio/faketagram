package com.example.faketagram.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.faketagram.R
import com.example.faketagram.data.model.User

@Composable
fun StartBlockedUserScreen(
    user: User,
    modifier: Modifier = Modifier,
    onUnblock: (User) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(top = 25.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.meelt_logo),
                    contentDescription = "App logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.BottomCenter),
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Has bloqueado a",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Text(
                text = user.username,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            val photoRes = if (user.resId != 0) user.resId else R.drawable.default_user
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .border(
                        width = 5.dp,
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(photoRes),
                    contentDescription = "Foto de ${user.username}",
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .aspectRatio(1f)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                BlockedActionItem(
                    icon = Icons.Rounded.Block,
                    label = "Esta cuenta no podrá enviarte mensajer ni entrar en tu perfil",
                    tint = MaterialTheme.colorScheme.tertiary,
                    onClick = {},
                    0.34f
                )
                BlockedActionItem(
                    icon = Icons.Rounded.ReportProblem,
                    label = "Puedes denunciar esta cuenta si crees que infringe las normas de la comunidad",
                    tint = MaterialTheme.colorScheme.tertiary,
                    onClick = {},
                    0.5f
                )
                BlockedActionItem(
                    icon = Icons.Rounded.LockOpen,
                    label = "Desbloquear usuario",
                    tint = MaterialTheme.colorScheme.tertiary,
                    onClick = { onUnblock(user) },
                    1f
                )
            }
        }
    }
}

@Composable
private fun BlockedActionItem(
    icon: ImageVector,
    label: String,
    tint: Color = Color.Gray,
    onClick: () -> Unit,
    fraction: Float
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth(fraction).clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

