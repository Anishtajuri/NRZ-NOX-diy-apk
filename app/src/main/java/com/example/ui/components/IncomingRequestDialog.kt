package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.localization.AppStrings
import com.example.model.LiveSnapRequest
import com.example.model.RequestType
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseDecline
import com.example.ui.theme.VioletSecondary

@Composable
fun IncomingRequestDialog(
    request: LiveSnapRequest,
    strings: AppStrings,
    onAllow: () -> Unit,
    onDecline: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val isPhoto = request.requestType == RequestType.PHOTO

    Dialog(
        onDismissRequest = { /* Require deliberate user action */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, if (isPhoto) EmeraldPrimary.copy(alpha = 0.5f) else VioletSecondary.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .testTag("incoming_request_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pulsing icon badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseScale)
                        .background(
                            if (isPhoto) EmeraldDark.copy(alpha = 0.2f) else VioletSecondary.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .border(
                            2.dp,
                            if (isPhoto) EmeraldPrimary else VioletSecondary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPhoto) request.requesterEmoji else "📱",
                        fontSize = 36.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isPhoto) strings.incomingPhotoTitle else strings.incomingScreenTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPhoto) EmeraldPrimary else VioletSecondary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isPhoto)
                        strings.incomingPhotoDesc(request.requesterName, request.requesterUsername)
                    else
                        strings.incomingScreenDesc(request.requesterName, request.requesterUsername),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Actions: Decline vs Allow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("decline_request_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = RoseDecline
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Decline",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(strings.decline, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onAllow,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("allow_request_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPhoto) EmeraldPrimary else VioletSecondary,
                            contentColor = if (isPhoto) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            if (isPhoto) Icons.Default.CameraAlt else Icons.Default.ScreenShare,
                            contentDescription = "Allow",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = if (isPhoto) strings.allowAndSnap else strings.allowScreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
