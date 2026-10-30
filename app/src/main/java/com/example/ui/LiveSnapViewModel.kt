package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.camera.CameraHelper
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.AppThemeMode
import com.example.model.ChatMessage
import com.example.model.FriendUser
import com.example.model.LiveSnapRequest
import com.example.model.LiveSnapResponse
import com.example.model.OutgoingState
import com.example.model.RequestStatus
import com.example.model.RequestType
import com.example.model.UserProfile
import com.example.network.NetworkUtils
import com.example.network.PeerClient
import com.example.network.PeerServer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class LiveSnapViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)
    private val peerClient = PeerClient()
    private val TAG = "LiveSnapViewModel"

    // Settings
    private val _appLanguage = MutableStateFlow(prefs.getAppLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _appTheme = MutableStateFlow(prefs.getAppTheme())
    val appTheme: StateFlow<AppThemeMode> = _appTheme.asStateFlow()

    val strings: AppStrings
        get() = AppStrings(_appLanguage.value)

    // Navigation (0: Friends, 1: Chat, 2: Settings)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Profile & Network
    private val _userProfile = MutableStateFlow(prefs.getUserProfile(NetworkUtils.getLocalIpAddress(application)))
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allFriends = MutableStateFlow(prefs.getSavedFriends())

    val filteredFriends: StateFlow<List<FriendUser>> = combine(_allFriends, _searchQuery) { friends, query ->
        if (query.isBlank()) {
            friends
        } else {
            val q = query.trim().lowercase().removePrefix("@")
            friends.filter {
                it.username.lowercase().contains(q) ||
                it.displayName.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Incoming Request State (when someone requests my photo or screen)
    private val _incomingRequest = MutableStateFlow<LiveSnapRequest?>(null)
    val incomingRequest: StateFlow<LiveSnapRequest?> = _incomingRequest.asStateFlow()

    // Camera Capture Modal State
    private val _isCameraModalOpen = MutableStateFlow(false)
    val isCameraModalOpen: StateFlow<Boolean> = _isCameraModalOpen.asStateFlow()

    // Screen Share State (when I am sharing MY screen to a friend)
    private val _isSharingMyScreen = MutableStateFlow(false)
    val isSharingMyScreen: StateFlow<Boolean> = _isSharingMyScreen.asStateFlow()

    private val _screenSharingRecipient = MutableStateFlow<String?>(null)
    val screenSharingRecipient: StateFlow<String?> = _screenSharingRecipient.asStateFlow()

    // Outgoing Request State (when I requested a photo or screen from a friend)
    private val _outgoingState = MutableStateFlow<OutgoingState>(OutgoingState.Idle)
    val outgoingState: StateFlow<OutgoingState> = _outgoingState.asStateFlow()

    // Ephemeral viewing countdown (10s down to 0)
    private val _ephemeralCountdown = MutableStateFlow(10)
    val ephemeralCountdown: StateFlow<Int> = _ephemeralCountdown.asStateFlow()
    private var countdownJob: Job? = null

    // Server State
    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

    // Chat State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "welcome-1",
                senderUsername = "sam_live",
                senderDisplayName = "Sam Rivera",
                senderEmoji = "😎",
                recipientUsername = "alex",
                text = "Hey! Send me a live photo request when you're free!",
                timestamp = System.currentTimeMillis() - 3600000
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _activeChatFriend = MutableStateFlow<FriendUser?>(null)
    val activeChatFriend: StateFlow<FriendUser?> = _activeChatFriend.asStateFlow()

    private var peerServer: PeerServer? = null

    init {
        refreshNetworkIp()
        startPeerServer()
    }

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun setAppLanguage(lang: AppLanguage) {
        _appLanguage.value = lang
        prefs.saveAppLanguage(lang)
    }

    fun setAppTheme(theme: AppThemeMode) {
        _appTheme.value = theme
        prefs.saveAppTheme(theme)
    }

    fun refreshNetworkIp() {
        val ip = NetworkUtils.getLocalIpAddress(getApplication())
        _userProfile.value = _userProfile.value.copy(ipAddress = ip)
    }

    private fun startPeerServer() {
        peerServer?.stop()
        val port = _userProfile.value.port
        peerServer = PeerServer(
            port = port,
            scope = viewModelScope,
            onRequestReceived = { req ->
                viewModelScope.launch {
                    handleIncomingRequestReceived(req)
                }
            },
            onResponseReceived = { resp ->
                viewModelScope.launch {
                    handleResponseReceived(resp)
                }
            },
            onScreenShareStopped = {
                viewModelScope.launch {
                    if (_outgoingState.value is OutgoingState.ScreenStreamActive) {
                        _outgoingState.value = OutgoingState.Idle
                    }
                }
            },
            onChatMessageReceived = { msg ->
                viewModelScope.launch {
                    _chatMessages.value = _chatMessages.value + msg
                }
            }
        )
        peerServer?.start()
        _isServerRunning.value = true
    }

    override fun onCleared() {
        super.onCleared()
        peerServer?.stop()
        countdownJob?.cancel()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Outgoing Requests ---

    fun sendPhotoRequest(friend: FriendUser) {
        val reqId = UUID.randomUUID().toString().substring(0, 8)
        _outgoingState.value = OutgoingState.Requesting(friend, reqId, RequestType.PHOTO)

        if (friend.isSimulated) {
            // Instant 1-second simulated response without lag
            viewModelScope.launch {
                delay(1200)
                if (_outgoingState.value is OutgoingState.Requesting) {
                    val selfie = CameraHelper.createFallbackSelfie(friend.displayName, friend.avatarEmoji)
                    val base64 = CameraHelper.bitmapToBase64(selfie)
                    displayReceivedPhoto(friend, base64)
                }
            }
        } else {
            viewModelScope.launch {
                val profile = _userProfile.value
                val snapRequest = LiveSnapRequest(
                    requestId = reqId,
                    requestType = RequestType.PHOTO,
                    requesterId = profile.id,
                    requesterUsername = profile.username,
                    requesterName = profile.displayName,
                    requesterEmoji = profile.avatarEmoji,
                    requesterHost = profile.ipAddress,
                    requesterPort = profile.port,
                    targetUsername = friend.username
                )

                val sent = peerClient.sendSnapRequest(friend.hostAddress, friend.port, snapRequest)
                if (!sent) {
                    _outgoingState.value = OutgoingState.Error("Could not reach ${friend.displayName} at ${friend.hostAddress}:${friend.port}. Make sure their phone is on.")
                }
            }
        }
    }

    fun sendScreenRequest(friend: FriendUser) {
        val reqId = UUID.randomUUID().toString().substring(0, 8)
        _outgoingState.value = OutgoingState.Requesting(friend, reqId, RequestType.SCREEN)

        if (friend.isSimulated) {
            // Simulated screen stream response
            viewModelScope.launch {
                delay(1200)
                if (_outgoingState.value is OutgoingState.Requesting) {
                    val screenFrame = CameraHelper.createScreenMirrorFrame(friend.displayName, friend.avatarEmoji)
                    val base64 = CameraHelper.bitmapToBase64(screenFrame)
                    _outgoingState.value = OutgoingState.ScreenStreamActive(friend, base64)
                }
            }
        } else {
            viewModelScope.launch {
                val profile = _userProfile.value
                val snapRequest = LiveSnapRequest(
                    requestId = reqId,
                    requestType = RequestType.SCREEN,
                    requesterId = profile.id,
                    requesterUsername = profile.username,
                    requesterName = profile.displayName,
                    requesterEmoji = profile.avatarEmoji,
                    requesterHost = profile.ipAddress,
                    requesterPort = profile.port,
                    targetUsername = friend.username
                )

                val sent = peerClient.sendSnapRequest(friend.hostAddress, friend.port, snapRequest)
                if (!sent) {
                    _outgoingState.value = OutgoingState.Error("Could not reach ${friend.displayName} for screen sharing.")
                }
            }
        }
    }

    fun cancelOutgoingRequest() {
        val current = _outgoingState.value
        if (current is OutgoingState.ScreenStreamActive) {
            viewModelScope.launch {
                peerClient.sendStopScreen(current.friend.hostAddress, current.friend.port)
            }
        }
        _outgoingState.value = OutgoingState.Idle
        countdownJob?.cancel()
    }

    // --- Incoming Requests ---

    private val _autoCaptureBanner = MutableStateFlow<String?>(null)
    val autoCaptureBanner: StateFlow<String?> = _autoCaptureBanner.asStateFlow()

    fun showAutoCaptureSuccess(message: String) {
        _autoCaptureBanner.value = message
        viewModelScope.launch {
            delay(4000)
            _autoCaptureBanner.value = null
        }
    }

    private fun handleIncomingRequestReceived(req: LiveSnapRequest) {
        _incomingRequest.value = req
    }

    fun acceptIncomingRequest(onAutoCapturePhoto: () -> Unit) {
        val req = _incomingRequest.value ?: return
        if (req.requestType == RequestType.PHOTO) {
            // User allowed live photo -> DO NOT open camera screen or camera app!
            // Execute automatic silent capture immediately
            onAutoCapturePhoto()
        } else {
            // User allowed screen share -> start sharing my screen
            _incomingRequest.value = null
            _isSharingMyScreen.value = true
            _screenSharingRecipient.value = req.requesterUsername

            viewModelScope.launch {
                val screenFrame = CameraHelper.createScreenMirrorFrame(_userProfile.value.displayName, _userProfile.value.avatarEmoji)
                val base64 = CameraHelper.bitmapToBase64(screenFrame)
                val response = LiveSnapResponse(
                    requestId = req.requestId,
                    requestType = RequestType.SCREEN,
                    responderUsername = _userProfile.value.username,
                    allowed = true,
                    imageBase64 = base64
                )
                peerClient.sendSnapResponse(req.requesterHost, req.requesterPort, response)
            }
        }
    }

    fun stopSharingMyScreen() {
        _isSharingMyScreen.value = false
        _screenSharingRecipient.value = null
        // Find recipient friend and notify
        val friend = _allFriends.value.firstOrNull { it.username == _screenSharingRecipient.value }
        if (friend != null) {
            viewModelScope.launch {
                peerClient.sendStopScreen(friend.hostAddress, friend.port)
            }
        }
    }

    fun declineIncomingRequest(reason: String = "User declined") {
        val req = _incomingRequest.value ?: return
        _incomingRequest.value = null
        _isCameraModalOpen.value = false

        viewModelScope.launch {
            val response = LiveSnapResponse(
                requestId = req.requestId,
                requestType = req.requestType,
                responderUsername = _userProfile.value.username,
                allowed = false,
                declineReason = reason
            )
            peerClient.sendSnapResponse(req.requesterHost, req.requesterPort, response)
        }
    }

    fun onPhotoCaptured(bitmap: Bitmap) {
        val req = _incomingRequest.value
        _isCameraModalOpen.value = false
        _incomingRequest.value = null

        val base64 = CameraHelper.bitmapToBase64(bitmap, quality = 65)

        if (req != null) {
            showAutoCaptureSuccess(strings.autoPhotoSentNotice(req.requesterUsername))
            viewModelScope.launch {
                val response = LiveSnapResponse(
                    requestId = req.requestId,
                    requestType = RequestType.PHOTO,
                    responderUsername = _userProfile.value.username,
                    allowed = true,
                    imageBase64 = base64
                )
                peerClient.sendSnapResponse(req.requesterHost, req.requesterPort, response)
            }
        }
    }

    fun closeCameraModalWithoutSending() {
        _isCameraModalOpen.value = false
        declineIncomingRequest("Camera capture cancelled")
    }

    // --- Response Handling ---

    private fun handleResponseReceived(response: LiveSnapResponse) {
        val current = _outgoingState.value
        if (current is OutgoingState.Requesting && current.requestId == response.requestId) {
            if (response.allowed) {
                if (response.requestType == RequestType.PHOTO && response.imageBase64 != null) {
                    displayReceivedPhoto(current.friend, response.imageBase64)
                } else if (response.requestType == RequestType.SCREEN) {
                    _outgoingState.value = OutgoingState.ScreenStreamActive(
                        current.friend,
                        response.imageBase64
                    )
                }
            } else {
                _outgoingState.value = OutgoingState.Declined(
                    current.friend,
                    response.declineReason ?: "Request declined"
                )
            }
        }
    }

    private fun displayReceivedPhoto(friend: FriendUser, imageBase64: String) {
        _outgoingState.value = OutgoingState.PhotoReceived(friend, imageBase64)
        startEphemeralCountdown()
    }

    private fun startEphemeralCountdown() {
        countdownJob?.cancel()
        _ephemeralCountdown.value = 10
        countdownJob = viewModelScope.launch {
            for (sec in 10 downTo 1) {
                _ephemeralCountdown.value = sec
                delay(1000)
            }
            _ephemeralCountdown.value = 0
            delay(800)
            _outgoingState.value = OutgoingState.Idle
        }
    }

    fun dismissPhotoViewer() {
        countdownJob?.cancel()
        _outgoingState.value = OutgoingState.Idle
    }

    // --- Simulation & Testing ---

    fun triggerSimulatedIncomingRequest(sender: FriendUser, type: RequestType = RequestType.PHOTO) {
        val req = LiveSnapRequest(
            requestId = UUID.randomUUID().toString().substring(0, 8),
            requestType = type,
            requesterId = sender.id,
            requesterUsername = sender.username,
            requesterName = sender.displayName,
            requesterEmoji = sender.avatarEmoji,
            requesterHost = "127.0.0.1",
            requesterPort = _userProfile.value.port,
            targetUsername = _userProfile.value.username
        )
        _incomingRequest.value = req
    }

    // --- Chat Management (No camera photos ever saved or stored) ---

    fun setActiveChatFriend(friend: FriendUser?) {
        _activeChatFriend.value = friend
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val targetFriend = _activeChatFriend.value ?: _allFriends.value.firstOrNull() ?: return
        val msg = ChatMessage(
            id = UUID.randomUUID().toString().substring(0, 8),
            senderUsername = _userProfile.value.username,
            senderDisplayName = _userProfile.value.displayName,
            senderEmoji = _userProfile.value.avatarEmoji,
            recipientUsername = targetFriend.username,
            text = text.trim(),
            timestamp = System.currentTimeMillis()
        )
        _chatMessages.value = _chatMessages.value + msg

        if (targetFriend.isSimulated) {
            // Simulated quick reply
            viewModelScope.launch {
                delay(1000)
                val reply = ChatMessage(
                    id = UUID.randomUUID().toString().substring(0, 8),
                    senderUsername = targetFriend.username,
                    senderDisplayName = targetFriend.displayName,
                    senderEmoji = targetFriend.avatarEmoji,
                    recipientUsername = _userProfile.value.username,
                    text = when ((1..3).random()) {
                        1 -> if (_appLanguage.value == AppLanguage.HINGLISH) "Haan bhai! Live photo request bhejo!" else "Awesome! Send me a live photo request!"
                        2 -> if (_appLanguage.value == AppLanguage.HINGLISH) "Main online hoon, bolo!" else "I'm online right now, let's connect!"
                        else -> if (_appLanguage.value == AppLanguage.HINGLISH) "Live camera ready hai :)" else "My front camera is ready :)"
                    },
                    timestamp = System.currentTimeMillis()
                )
                _chatMessages.value = _chatMessages.value + reply
            }
        } else {
            viewModelScope.launch {
                peerClient.sendChatMessage(targetFriend.hostAddress, targetFriend.port, msg)
            }
        }
    }

    // --- Profile & Friends Management ---

    fun updateProfile(newUsername: String, newDisplayName: String, newEmoji: String) {
        val updated = _userProfile.value.copy(
            username = newUsername.trim().removePrefix("@"),
            displayName = newDisplayName.trim(),
            avatarEmoji = newEmoji
        )
        _userProfile.value = updated
        prefs.saveUserProfile(updated)
    }

    fun addFriend(username: String, displayName: String, hostAddress: String, port: Int) {
        val cleanUser = username.trim().removePrefix("@")
        val cleanName = if (displayName.isBlank()) cleanUser.replaceFirstChar { it.uppercase() } else displayName.trim()
        val cleanHost = if (hostAddress.isBlank()) "127.0.0.1" else hostAddress.trim()
        val newFriend = FriendUser(
            id = UUID.randomUUID().toString().substring(0, 8),
            username = cleanUser,
            displayName = cleanName,
            avatarEmoji = "📸",
            hostAddress = cleanHost,
            port = if (port > 0) port else 8990,
            isOnline = true,
            isSimulated = false,
            statusText = "Local peer ($cleanHost)"
        )
        val updated = _allFriends.value + newFriend
        _allFriends.value = updated
        prefs.saveFriends(updated)
    }

    fun removeFriend(friendId: String) {
        val updated = _allFriends.value.filterNot { it.id == friendId }
        _allFriends.value = updated
        prefs.saveFriends(updated)
    }
}
