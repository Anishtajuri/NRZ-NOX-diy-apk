package com.example.network

import android.util.Log
import com.example.model.ChatMessage
import com.example.model.LiveSnapRequest
import com.example.model.LiveSnapResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PeerClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val TAG = "PeerClient"

    suspend fun pingPeer(host: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("http://$host:$port/api/ping")
                .get()
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendSnapRequest(host: String, port: Int, snapRequest: LiveSnapRequest): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("requestId", snapRequest.requestId)
                    put("requestType", snapRequest.requestType.name)
                    put("requesterId", snapRequest.requesterId)
                    put("requesterUsername", snapRequest.requesterUsername)
                    put("requesterName", snapRequest.requesterName)
                    put("requesterEmoji", snapRequest.requesterEmoji)
                    put("requesterHost", snapRequest.requesterHost)
                    put("requesterPort", snapRequest.requesterPort)
                    put("targetUsername", snapRequest.targetUsername)
                    put("timestamp", snapRequest.timestamp)
                }

                val body = json.toString().toRequestBody(jsonMediaType)
                val request = Request.Builder()
                    .url("http://$host:$port/api/request_snap")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val success = response.isSuccessful
                response.close()
                success
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send snap request to $host:$port", e)
                false
            }
        }

    suspend fun sendSnapResponse(host: String, port: Int, snapResponse: LiveSnapResponse): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("requestId", snapResponse.requestId)
                    put("requestType", snapResponse.requestType.name)
                    put("responderUsername", snapResponse.responderUsername)
                    put("allowed", snapResponse.allowed)
                    if (snapResponse.imageBase64 != null) {
                        put("imageBase64", snapResponse.imageBase64)
                    }
                    if (snapResponse.declineReason != null) {
                        put("declineReason", snapResponse.declineReason)
                    }
                    put("timestamp", snapResponse.timestamp)
                }

                val body = json.toString().toRequestBody(jsonMediaType)
                val request = Request.Builder()
                    .url("http://$host:$port/api/respond_snap")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val success = response.isSuccessful
                response.close()
                success
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send snap response to $host:$port", e)
                false
            }
        }

    suspend fun sendStopScreen(host: String, port: Int): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val body = "{}".toRequestBody(jsonMediaType)
                val request = Request.Builder()
                    .url("http://$host:$port/api/stop_screen")
                    .post(body)
                    .build()
                val response = client.newCall(request).execute()
                val success = response.isSuccessful
                response.close()
                success
            } catch (e: Exception) {
                false
            }
        }

    suspend fun sendChatMessage(host: String, port: Int, chatMessage: ChatMessage): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("id", chatMessage.id)
                    put("senderUsername", chatMessage.senderUsername)
                    put("senderDisplayName", chatMessage.senderDisplayName)
                    put("senderEmoji", chatMessage.senderEmoji)
                    put("recipientUsername", chatMessage.recipientUsername)
                    put("text", chatMessage.text)
                    put("timestamp", chatMessage.timestamp)
                }
                val body = json.toString().toRequestBody(jsonMediaType)
                val request = Request.Builder()
                    .url("http://$host:$port/api/chat")
                    .post(body)
                    .build()
                val response = client.newCall(request).execute()
                val success = response.isSuccessful
                response.close()
                success
            } catch (e: Exception) {
                false
            }
        }
}
