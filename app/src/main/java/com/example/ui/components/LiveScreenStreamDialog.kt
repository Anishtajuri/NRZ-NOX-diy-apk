package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.camera.CameraHelper
import com.example.localization.AppStrings
import com.example.model.FriendUser
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.RoseDecline
import com.example.ui.theme.VioletSecondary

@Composable
fun LiveScreenStreamDialog(
    friend: FriendUser,
    frameBase64: String?,
    strings: AppStrings,
    onClose: () -> Unit
) {
    val bitmap = remember(frameBase64) {
        if (frameBase64 != null) {
            CameraHelper.base64ToBitmap(frameBase64)
        } else {
            CameraHelper.createScreenMirrorFrame(friend.displayName, friend.avatarEmoji)
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBg)
                .testTag("live_screen_stream_dialog"),
            color = ObsidianBg
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Mobile screen frame container
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp, bottom = 30.dp, start = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.dp, VioletSecondary.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Live screen stream from ${friend.displayName}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Top Header with Live Screen Badge and Close Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp, start = 20.dp, end = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(RoseDecline, CircleShape)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "🔴 LIVE SCREEN • @${friend.username}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.75f), CircleShape)
                            .testTag("close_screen_view_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Bottom Stop / Close Action
                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .testTag("bottom_stop_screen_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDecline, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.closeScreenView, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
