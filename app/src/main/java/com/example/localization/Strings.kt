package com.example.localization

enum class AppLanguage(val displayName: String) {
    ENGLISH("English"),
    HINGLISH("Hinglish (Hindi in English letters)")
}

enum class AppThemeMode(val displayName: String) {
    DARK("Dark Mode"),
    WHITE("White Mode"),
    NORMAL("Normal Mode")
}

class AppStrings(private val language: AppLanguage) {

    val appName: String
        get() = "NRZ NOX"

    val tabFriends: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Friends"
            AppLanguage.HINGLISH -> "Dost"
        }

    val tabChat: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Chat"
            AppLanguage.HINGLISH -> "Baatcheet"
        }

    val tabSettings: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Settings"
            AppLanguage.HINGLISH -> "Settings"
        }

    val searchPlaceholder: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Search friends by username or name..."
            AppLanguage.HINGLISH -> "Username ya naam se dost dhoondein..."
        }

    val friendsCount: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "FRIENDS & PEERS"
            AppLanguage.HINGLISH -> "AAPKE DOST"
        }

    val livePhotoBtn: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Live Photo"
            AppLanguage.HINGLISH -> "Live Photo"
        }

    val liveScreenBtn: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Live Screen"
            AppLanguage.HINGLISH -> "Live Screen"
        }

    val testBannerTitle: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "⚡ Test Instant Incoming Pop-up"
            AppLanguage.HINGLISH -> "⚡ Test Incoming Pop-up"
        }

    val testBannerDesc: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Simulate an instant live request to test the front camera pop-up & screen view without delay."
            AppLanguage.HINGLISH -> "Ek test request bhej kar instant front camera pop-up aur screen view check karein."
        }

    val incomingPhotoTitle: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "🚨 Live Photo Request"
            AppLanguage.HINGLISH -> "🚨 Live Photo Request Aaya Hai"
        }

    fun incomingPhotoDesc(name: String, username: String): String = when (language) {
        AppLanguage.ENGLISH -> "$name (@$username) is asking for a live photo of your face right now! Allow front camera?"
        AppLanguage.HINGLISH -> "$name (@$username) abhi aapka live face dekhna chahte hain! Front camera allow karein?"
    }

    val incomingScreenTitle: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "📱 Live Screen Request"
            AppLanguage.HINGLISH -> "📱 Live Screen Request Aaya Hai"
        }

    fun incomingScreenDesc(name: String, username: String): String = when (language) {
        AppLanguage.ENGLISH -> "$name (@$username) is requesting to view your phone screen live right now. Allow screen sharing?"
        AppLanguage.HINGLISH -> "$name (@$username) aapke phone ki live screen dekhna chahte hain. Screen share allow karein?"
    }

    val allowAndSnap: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Allow & Snap"
            AppLanguage.HINGLISH -> "Allow Karein"
        }

    val allowScreen: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Allow Screen"
            AppLanguage.HINGLISH -> "Screen Dikhayein"
        }

    val decline: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Decline"
            AppLanguage.HINGLISH -> "Mana Karein"
        }

    val screenSharingActive: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "🔴 You are sharing your screen live"
            AppLanguage.HINGLISH -> "🔴 Aapki phone screen live share ho rahi hai"
        }

    val stopSharing: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Stop Sharing"
            AppLanguage.HINGLISH -> "Screen Share Rokein"
        }

    val viewScreenTitle: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Live Screen Stream"
            AppLanguage.HINGLISH -> "Live Screen Stream"
        }

    val closeScreenView: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Close Screen View"
            AppLanguage.HINGLISH -> "Screen Band Karein"
        }

    val chatPrivacyNotice: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "🔒 Privacy: Camera photos are NEVER saved in chats."
            AppLanguage.HINGLISH -> "🔒 Privacy: Camera photos chats mein KABHI save nahi hote."
        }

    val chatPlaceholder: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Type a message..."
            AppLanguage.HINGLISH -> "Message likhein..."
        }

    val chatEmpty: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "No messages yet. Send a hello!"
            AppLanguage.HINGLISH -> "Koi message nahi hai. Hello bhejein!"
        }

    val send: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Send"
            AppLanguage.HINGLISH -> "Bhejein"
        }

    val settingsLanguage: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "App Language"
            AppLanguage.HINGLISH -> "App Ki Bhasha"
        }

    val settingsTheme: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "App Theme"
            AppLanguage.HINGLISH -> "App Ka Theme"
        }

    val settingsProfile: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Profile & Username"
            AppLanguage.HINGLISH -> "Aapka Profile & Username"
        }

    val settingsNetwork: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Peer Connection & IP"
            AppLanguage.HINGLISH -> "Peer Connection & IP"
        }

    val photoVanishesIn: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Photo vanishes in"
            AppLanguage.HINGLISH -> "Photo gayab ho jayegi"
        }

    val photoExpired: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Photo Expired"
            AppLanguage.HINGLISH -> "Photo Expire Ho Gayi"
        }

    val photoExpiredDesc: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Live photos are strictly ephemeral and disappear after viewing."
            AppLanguage.HINGLISH -> "Live photos sirf dekhne ke liye hoti hain aur turant gayab ho jaati hain."
        }

    val waitingTitle: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Waiting for response..."
            AppLanguage.HINGLISH -> "Jawab ka intezar hai..."
        }

    val cancel: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Cancel"
            AppLanguage.HINGLISH -> "Cancel Karein"
        }

    val addFriend: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "Add Friend"
            AppLanguage.HINGLISH -> "Dost Add Karein"
        }

    fun autoPhotoSentNotice(username: String): String = when (language) {
        AppLanguage.ENGLISH -> "✓ Front photo automatically captured & sent to @$username!"
        AppLanguage.HINGLISH -> "✓ Front photo automatically click hokar @$username ko chala gaya!"
    }

    val autoPhotoSentSubtitle: String
        get() = when (language) {
            AppLanguage.ENGLISH -> "No camera screen opened. Transmitted automatically with your consent."
            AppLanguage.HINGLISH -> "Camera screen nahi khula. Permission ke sath automatically chala gaya."
        }
}
