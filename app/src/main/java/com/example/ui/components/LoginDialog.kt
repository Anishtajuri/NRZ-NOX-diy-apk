package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.VioletSecondary

enum class LoginMethod {
    PHONE_OTP,
    EMAIL
}

@Composable
fun LoginDialog(
    onLoginSuccess: (identifier: String) -> Unit,
    onDismiss: () -> Unit
) {
    var method by remember { mutableStateOf(LoginMethod.PHONE_OTP) }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .testTag("login_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close & title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sign In (Optional)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_login_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Sign in with phone or email to sync your handle across devices, or skip and continue as guest.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Tab Switcher: Phone OTP vs Email
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (method == LoginMethod.PHONE_OTP) EmeraldPrimary else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { method = LoginMethod.PHONE_OTP }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Phone & OTP",
                            fontWeight = FontWeight.Bold,
                            color = if (method == LoginMethod.PHONE_OTP) Color.Black else MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (method == LoginMethod.EMAIL) EmeraldPrimary else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { method = LoginMethod.EMAIL }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Email Login",
                            fontWeight = FontWeight.Bold,
                            color = if (method == LoginMethod.EMAIL) Color.Black else MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (method == LoginMethod.PHONE_OTP) {
                    // Phone Number Input
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it.filter { c -> c.isDigit() }.take(10) },
                        label = { Text("Phone Number") },
                        prefix = { Text("+91 ") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("login_phone_input"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !otpSent
                    )

                    if (!otpSent) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (phoneNumber.length >= 10) {
                                    otpSent = true
                                    otpCode = "123456" // Auto-fill sample OTP for convenience
                                    feedbackMessage = "OTP sent to +91 $phoneNumber! (Demo OTP: 123456)"
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("send_otp_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            enabled = phoneNumber.length >= 10
                        ) {
                            Text("Send OTP", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { otpCode = it.filter { c -> c.isDigit() }.take(6) },
                            label = { Text("Enter 6-Digit OTP") },
                            leadingIcon = { Icon(Icons.Default.Shield, contentDescription = "OTP") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("login_otp_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (otpCode.length == 6) {
                                    onLoginSuccess("+91 $phoneNumber")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("verify_otp_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            enabled = otpCode.length == 6
                        ) {
                            Text("Verify & Sign In", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Email & Password Input
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth().testTag("login_email_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (email.contains("@") && password.length >= 4) {
                                onLoginSuccess(email)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("confirm_email_login_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        enabled = email.contains("@") && password.length >= 4
                    ) {
                        Text("Sign In with Email", fontWeight = FontWeight.Bold)
                    }
                }

                feedbackMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = msg,
                        color = EmeraldPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Guest / Skip button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("continue_as_guest_btn")
                ) {
                    Text(
                        text = "Continue as Guest without login",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
