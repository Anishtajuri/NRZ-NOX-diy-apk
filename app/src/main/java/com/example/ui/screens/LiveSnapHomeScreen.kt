package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.camera.SilentCameraCapture
import com.example.localization.AppStrings
import com.example.model.FriendUser
import com.example.model.OutgoingState
import com.example.model.RequestType
import com.example.ui.LiveSnapViewModel
import com.example.ui.components.AddFriendDialog
import com.example.ui.components.EphemeralPhotoViewerDialog
import com.example.ui.components.IncomingRequestDialog
import com.example.ui.components.LiveScreenStreamDialog
import com.example.ui.components.OutgoingRequestWaitingDialog
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseDecline
import com.example.ui.theme.VioletSecondary

@Composable
fun LiveSnapHomeScreen(
    viewModel: LiveSnapViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val strings = viewModel.strings
    val selectedTab by viewModel.selectedTab.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val friends by viewModel.filteredFriends.collectAsState()
    val incomingRequest by viewModel.incomingRequest.collectAsState()
    val outgoingState by viewModel.outgoingState.collectAsState()
    val countdownSeconds by viewModel.ephemeralCountdown.collectAsState()
    val isServerRunning by viewModel.isServerRunning.collectAsState()
    val isSharingMyScreen by viewModel.isSharingMyScreen.collectAsState()
    val screenRecipient by viewModel.screenSharingRecipient.collectAsState()
    val autoCaptureBanner by viewModel.autoCaptureBanner.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted && incomingRequest?.requestType == RequestType.PHOTO) {
            // Once permitted, capture silently in background without opening camera UI
            SilentCameraCapture.captureSilentFrontPhoto(
                context = context,
                lifecycleOwner = lifecycleOwner,
                fallbackUsername = userProfile.displayName,
                fallbackEmoji = userProfile.avatarEmoji,
                onSuccess = { bmp -> viewModel.onPhotoCaptured(bmp) }
            )
        }
    }

    var showAddFriendDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    icon = { Icon(Icons.Default.People, contentDescription = strings.tabFriends) },
                    label = { Text(strings.tabFriends) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary
                    ),
                    modifier = Modifier.testTag("nav_friends_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    icon = { Icon(Icons.Default.Chat, contentDescription = strings.tabChat) },
                    label = { Text(strings.tabChat) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary
                    ),
                    modifier = Modifier.testTag("nav_chat_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = strings.tabSettings) },
                    label = { Text(strings.tabSettings) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary
                    ),
                    modifier = Modifier.testTag("nav_settings_tab")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddFriendDialog = true },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier.testTag("add_friend_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = strings.addFriend)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Sharing Active Persistent Banner (if currently sharing)
            if (isSharingMyScreen) {
                Surface(
                    color = RoseDecline,
                    modifier = Modifier.fillMaxWidth().testTag("sharing_active_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.White, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.screenSharingActive + (screenRecipient?.let { " (@$it)" } ?: ""),
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { viewModel.stopSharingMyScreen() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = RoseDecline
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("stop_sharing_banner_btn")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.stopSharing, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Top Header Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.nrz_nox_logo),
                                contentDescription = "NRZ NOX Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = strings.appName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Profile Chip
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = userProfile.avatarEmoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "@${userProfile.username}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Auto Capture Confirmation Banner (when front photo is automatically sent)
            AnimatedVisibility(
                visible = autoCaptureBanner != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Surface(
                    color = EmeraldDark,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("auto_photo_sent_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📸", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = autoCaptureBanner ?: "",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = strings.autoPhotoSentSubtitle,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Main Content Area switched by Tab
            when (selectedTab) {
                0 -> {
                    // FRIENDS TAB
                    FriendsTabContent(
                        strings = strings,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        friends = friends,
                        isServerRunning = isServerRunning,
                        userIp = userProfile.ipAddress,
                        userPort = userProfile.port,
                        onRequestPhoto = { friend -> viewModel.sendPhotoRequest(friend) },
                        onRequestScreen = { friend -> viewModel.sendScreenRequest(friend) },
                        onTestIncoming = { type ->
                            val sample = friends.firstOrNull() ?: FriendUser("sim", "sam", "Sam")
                            viewModel.triggerSimulatedIncomingRequest(sample, type)
                        }
                    )
                }
                1 -> {
                    // CHATTING TAB
                    ChatScreen(viewModel = viewModel)
                }
                2 -> {
                    // SETTINGS TAB
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // --- Active Dynamic Pop-ups & Views ---

    // 1. Incoming Request Pop-up (Photo or Screen)
    incomingRequest?.let { req ->
        IncomingRequestDialog(
            request = req,
            strings = strings,
            onAllow = {
                if (req.requestType == RequestType.PHOTO) {
                    if (hasCameraPermission) {
                        // AUTOMATIC CAPTURE WITHOUT OPENING CAMERA SCREEN OR CAMERA APP
                        SilentCameraCapture.captureSilentFrontPhoto(
                            context = context,
                            lifecycleOwner = lifecycleOwner,
                            fallbackUsername = userProfile.displayName,
                            fallbackEmoji = userProfile.avatarEmoji,
                            onSuccess = { bmp -> viewModel.onPhotoCaptured(bmp) }
                        )
                    } else {
                        // Request permission; once granted, listener will auto capture
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                } else {
                    viewModel.acceptIncomingRequest { /* not used for screen */ }
                }
            },
            onDecline = { viewModel.declineIncomingRequest() }
        )
    }

    // 3. Outgoing Request Waiting Dialog
    if (outgoingState is OutgoingState.Requesting ||
        outgoingState is OutgoingState.Declined ||
        outgoingState is OutgoingState.Error
    ) {
        OutgoingRequestWaitingDialog(
            outgoingState = outgoingState,
            onCancel = { viewModel.cancelOutgoingRequest() }
        )
    }

    // 4. Ephemeral Live Photo Viewer (10s countdown)
    if (outgoingState is OutgoingState.PhotoReceived) {
        val received = outgoingState as OutgoingState.PhotoReceived
        EphemeralPhotoViewerDialog(
            friend = received.friend,
            imageBase64 = received.imageBase64,
            countdownSeconds = countdownSeconds,
            onDismiss = { viewModel.dismissPhotoViewer() }
        )
    }

    // 5. Live Screen Stream Viewer (when watching friend's screen live)
    if (outgoingState is OutgoingState.ScreenStreamActive) {
        val stream = outgoingState as OutgoingState.ScreenStreamActive
        LiveScreenStreamDialog(
            friend = stream.friend,
            frameBase64 = stream.streamFrameBase64,
            strings = strings,
            onClose = { viewModel.cancelOutgoingRequest() }
        )
    }

    // 6. Add Friend Dialog
    if (showAddFriendDialog) {
        AddFriendDialog(
            onAdd = { username, name, host, port ->
                viewModel.addFriend(username, name, host, port)
                showAddFriendDialog = false
            },
            onDismiss = { showAddFriendDialog = false }
        )
    }
}

@Composable
fun FriendsTabContent(
    strings: AppStrings,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    friends: List<FriendUser>,
    isServerRunning: Boolean,
    userIp: String,
    userPort: Int,
    onRequestPhoto: (FriendUser) -> Unit,
    onRequestScreen: (FriendUser) -> Unit,
    onTestIncoming: (RequestType) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text(strings.searchPlaceholder) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("search_friends_input"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        // Instant Pop-up Test Simulator Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = VioletSecondary.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .border(1.dp, VioletSecondary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = strings.testBannerTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = VioletSecondary
                )
                Text(
                    text = strings.testBannerDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onTestIncoming(RequestType.PHOTO) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).testTag("test_photo_popup_btn")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Photo Test", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Photo Pop-up", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onTestIncoming(RequestType.SCREEN) },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletSecondary, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).testTag("test_screen_popup_btn")
                    ) {
                        Icon(Icons.Default.ScreenShare, contentDescription = "Screen Test", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Screen Pop-up", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Friends Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${strings.friendsCount} (${friends.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        // Friends List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(friends, key = { it.id }) { friend ->
                FriendCardRow(
                    friend = friend,
                    strings = strings,
                    onRequestPhoto = { onRequestPhoto(friend) },
                    onRequestScreen = { onRequestScreen(friend) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
fun FriendCardRow(
    friend: FriendUser,
    strings: AppStrings,
    onRequestPhoto: () -> Unit,
    onRequestScreen: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("friend_card_${friend.username}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = friend.avatarEmoji, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = friend.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "@${friend.username}",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${friend.statusText}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Live Photo & Live Screen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRequestPhoto,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).testTag("request_photo_btn_${friend.username}")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Photo", modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.livePhotoBtn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRequestScreen,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VioletSecondary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).testTag("request_screen_btn_${friend.username}")
                ) {
                    Icon(Icons.Default.ScreenShare, contentDescription = "Screen", modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.liveScreenBtn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
