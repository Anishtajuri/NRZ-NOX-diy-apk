package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.camera.CameraHelper
import com.example.camera.CameraPreview
import com.example.camera.capturePhoto
import com.example.model.LiveSnapRequest
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.RoseDecline
import kotlinx.coroutines.delay

@Composable
fun LiveCameraCaptureModal(
    request: LiveSnapRequest?,
    targetUsername: String,
    onPhotoConfirmed: (Bitmap) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var imageCaptureInstance by remember { mutableStateOf<ImageCapture?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var countdownSeconds by remember { mutableIntStateOf(3) }
    var isCountdownActive by remember { mutableStateOf(true) }

    LaunchedEffect(hasCameraPermission, isCountdownActive) {
        if (hasCameraPermission && isCountdownActive) {
            while (countdownSeconds > 0) {
                delay(1000)
                countdownSeconds -= 1
            }
            // Auto capture on countdown 0
            if (capturedBitmap == null) {
                capturePhoto(
                    context = context,
                    imageCapture = imageCaptureInstance,
                    fallbackUsername = "Me",
                    fallbackEmoji = "📸",
                    onSuccess = { bmp -> capturedBitmap = bmp },
                    onError = {
                        val fallback = CameraHelper.createFallbackSelfie("Me", "📸")
                        capturedBitmap = fallback
                    }
                )
            }
        }
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBg)
                .testTag("camera_capture_modal"),
            color = ObsidianBg
        ) {
            if (!hasCameraPermission) {
                // Permission Request Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .background(EmeraldDark.copy(alpha = 0.2f), CircleShape)
                            .border(2.dp, EmeraldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Camera Required",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "To send a real-time live photo from your front camera to $targetUsername, please allow camera access.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .testTag("grant_camera_permission_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Grant Camera Permission", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            // Use live fallback selfie for testing/emulator without physical camera
                            val fallback = CameraHelper.createFallbackSelfie("Me", "😎")
                            capturedBitmap = fallback
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .testTag("use_avatar_selfie_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simulate Live Selfie")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.textButtonColors(contentColor = RoseDecline)
                    ) {
                        Text("Cancel")
                    }
                }
            } else if (capturedBitmap == null) {
                // Live Camera Viewfinder & Countdown
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        onImageCaptureReady = { ic -> imageCaptureInstance = ic },
                        onCameraUnavailable = {
                            val fallback = CameraHelper.createFallbackSelfie("Me", "📸")
                            capturedBitmap = fallback
                        }
                    )

                    // Top Bar with Cancel button & Friend Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "Live for @$targetUsername",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        IconButton(
                            onClick = onCancel,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .testTag("cancel_camera_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    // Centered Countdown overlay
                    AnimatedVisibility(
                        visible = isCountdownActive && countdownSeconds > 0,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                .border(3.dp, EmeraldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$countdownSeconds",
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldPrimary
                            )
                        }
                    }

                    // Bottom Controls: Manual snap button
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (countdownSeconds > 0) "Snapping in $countdownSeconds..." else "Smile! 📸",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Shutter Button
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .border(4.dp, Color.White, CircleShape)
                                .padding(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    isCountdownActive = false
                                    capturePhoto(
                                        context = context,
                                        imageCapture = imageCaptureInstance,
                                        fallbackUsername = "Me",
                                        fallbackEmoji = "📸",
                                        onSuccess = { bmp -> capturedBitmap = bmp },
                                        onError = {
                                            val fallback = CameraHelper.createFallbackSelfie("Me", "📸")
                                            capturedBitmap = fallback
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("take_photo_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = CircleShape
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Take Photo",
                                    tint = Color.Black
                                )
                            }
                        }
                    }
                }
            } else {
                // Photo Review & Send Confirmation
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Send this live photo?",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Will be shown to @$targetUsername for 10 seconds",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Captured Photo Display
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(2.dp, EmeraldPrimary.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    ) {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Captured Live Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                capturedBitmap = null
                                countdownSeconds = 3
                                isCountdownActive = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("retake_photo_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retake")
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Retake")
                        }

                        Button(
                            onClick = { onPhotoConfirmed(capturedBitmap!!) },
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("confirm_send_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Send")
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Confirm & Send", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
