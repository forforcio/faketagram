package com.izzo.meelt.ui.nav

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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.izzo.meelt.R
import com.izzo.meelt.data.model.User

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
                    .height(70.dp)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(top = 25.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.meelt_logo_small),
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
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = stringResource(R.string.blocked_title),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 25.sp
            )

            Text(
                text = user.username,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Light,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            val photoRes = if (user.resId != 0) user.resId else R.drawable.default_user
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.70f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .border(
                        width = 5.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(photoRes),
                    contentDescription = stringResource(R.string.content_desc_photo_of_user, user.username),
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
                    label = stringResource(R.string.blocked_action_block_desc),
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = {},
                    0.34f
                )
                BlockedActionItem(
                    icon = Icons.Rounded.ReportProblem,
                    label = stringResource(R.string.blocked_action_report_desc),
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = {},
                    0.5f
                )
                BlockedActionItem(
                    icon = Icons.Rounded.LockOpen,
                    label = stringResource(R.string.blocked_action_unblock),
                    tint = MaterialTheme.colorScheme.primary,
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
                .padding(horizontal = 8.dp)
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
                fontWeight = FontWeight.Light,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 17.sp
            )
        }
    }
}

