package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.localization.AppLanguage
import com.example.localization.AppThemeMode
import com.example.model.FriendUser
import com.example.model.UserProfile
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("livesnap_prefs", Context.MODE_PRIVATE)

    fun getAppLanguage(): AppLanguage {
        val code = prefs.getString("app_language", AppLanguage.ENGLISH.name)
        return try {
            AppLanguage.valueOf(code ?: AppLanguage.ENGLISH.name)
        } catch (e: Exception) {
            AppLanguage.ENGLISH
        }
    }

    fun saveAppLanguage(language: AppLanguage) {
        prefs.edit().putString("app_language", language.name).apply()
    }

    fun getAppTheme(): AppThemeMode {
        val code = prefs.getString("app_theme", AppThemeMode.DARK.name)
        return try {
            AppThemeMode.valueOf(code ?: AppThemeMode.DARK.name)
        } catch (e: Exception) {
            AppThemeMode.DARK
        }
    }

    fun saveAppTheme(theme: AppThemeMode) {
        prefs.edit().putString("app_theme", theme.name).apply()
    }

    fun getUserProfile(defaultIp: String): UserProfile {
        val id = prefs.getString("user_id", null) ?: run {
            val newId = UUID.randomUUID().toString().substring(0, 8)
            prefs.edit().putString("user_id", newId).apply()
            newId
        }
        val username = prefs.getString("username", "alex") ?: "alex"
        val displayName = prefs.getString("display_name", "Alex") ?: "Alex"
        val emoji = prefs.getString("avatar_emoji", "⚡") ?: "⚡"
        val port = prefs.getInt("port", 8990)

        return UserProfile(
            id = id,
            username = username,
            displayName = displayName,
            avatarEmoji = emoji,
            ipAddress = defaultIp,
            port = port
        )
    }

    fun saveUserProfile(profile: UserProfile) {
        prefs.edit()
            .putString("username", profile.username.trim().removePrefix("@"))
            .putString("display_name", profile.displayName.trim())
            .putString("avatar_emoji", profile.avatarEmoji)
            .putInt("port", profile.port)
            .apply()
    }

    fun getSavedFriends(): List<FriendUser> {
        val raw = prefs.getString("saved_friends", null)
        if (raw == null) {
            val defaultFriends = listOf(
                FriendUser(
                    id = "sam-1",
                    username = "sam_live",
                    displayName = "Sam Rivera",
                    avatarEmoji = "😎",
                    hostAddress = "127.0.0.1",
                    port = 8990,
                    isOnline = true,
                    isSimulated = true,
                    statusText = "Ready for live snaps"
                ),
                FriendUser(
                    id = "taylor-2",
                    username = "taylor_vibe",
                    displayName = "Taylor Swiftness",
                    avatarEmoji = "✨",
                    hostAddress = "127.0.0.1",
                    port = 8990,
                    isOnline = true,
                    isSimulated = true,
                    statusText = "Chilling outdoors"
                ),
                FriendUser(
                    id = "jordan-3",
                    username = "jordan_k",
                    displayName = "Jordan Lee",
                    avatarEmoji = "🚀",
                    hostAddress = "127.0.0.1",
                    port = 8990,
                    isOnline = true,
                    isSimulated = true,
                    statusText = "At coffee shop"
                )
            )
            saveFriends(defaultFriends)
            return defaultFriends
        }

        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<FriendUser>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    FriendUser(
                        id = obj.getString("id"),
                        username = obj.getString("username"),
                        displayName = obj.getString("displayName"),
                        avatarEmoji = obj.optString("avatarEmoji", "👤"),
                        hostAddress = obj.optString("hostAddress", "127.0.0.1"),
                        port = obj.optInt("port", 8990),
                        isOnline = obj.optBoolean("isOnline", true),
                        isSimulated = obj.optBoolean("isSimulated", false),
                        statusText = obj.optString("statusText", "Online")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveFriends(friends: List<FriendUser>) {
        val array = JSONArray()
        for (f in friends) {
            val obj = JSONObject().apply {
                put("id", f.id)
                put("username", f.username)
                put("displayName", f.displayName)
                put("avatarEmoji", f.avatarEmoji)
                put("hostAddress", f.hostAddress)
                put("port", f.port)
                put("isOnline", f.isOnline)
                put("isSimulated", f.isSimulated)
                put("statusText", f.statusText)
            }
            array.put(obj)
        }
        prefs.edit().putString("saved_friends", array.toString()).apply()
    }
}
