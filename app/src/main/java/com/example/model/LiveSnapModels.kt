package com.example.model

enum class RequestType {
    PHOTO,
    SCREEN
}

enum class RequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    EXPIRED,
    CANCELLED
}

data class UserProfile(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarEmoji: String = "⚡",
    val ipAddress: String = "127.0.0.1",
    val port: Int = 8990
)

data class FriendUser(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarEmoji: String = "👤",
    val hostAddress: String = "127.0.0.1",
    val port: Int = 8990,
    val isOnline: Boolean = true,
    val isSimulated: Boolean = false,
    val statusText: String = "Online & Ready"
)

data class LiveSnapRequest(
    val requestId: String,
    val requestType: RequestType = RequestType.PHOTO,
    val requesterId: String,
    val requesterUsername: String,
    val requesterName: String,
    val requesterEmoji: String = "⚡",
    val requesterHost: String = "127.0.0.1",
    val requesterPort: Int = 8990,
    val targetUsername: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: RequestStatus = RequestStatus.PENDING
)

data class LiveSnapResponse(
    val requestId: String,
    val requestType: RequestType = RequestType.PHOTO,
    val responderUsername: String,
    val allowed: Boolean,
    val imageBase64: String? = null,
    val declineReason: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class OutgoingState {
    object Idle : OutgoingState()
    data class Requesting(
        val friend: FriendUser,
        val requestId: String,
        val type: RequestType = RequestType.PHOTO
    ) : OutgoingState()
    data class Declined(val friend: FriendUser, val reason: String) : OutgoingState()
    data class PhotoReceived(
        val friend: FriendUser,
        val imageBase64: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : OutgoingState()
    data class ScreenStreamActive(
        val friend: FriendUser,
        val streamFrameBase64: String? = null
    ) : OutgoingState()
    data class Error(val message: String) : OutgoingState()
}

data class ChatMessage(
    val id: String,
    val senderUsername: String,
    val senderDisplayName: String,
    val senderEmoji: String = "👤",
    val recipientUsername: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
