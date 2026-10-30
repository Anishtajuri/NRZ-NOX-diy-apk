package com.example.network

import android.util.Log
import com.example.model.ChatMessage
import com.example.model.LiveSnapRequest
import com.example.model.LiveSnapResponse
import com.example.model.RequestStatus
import com.example.model.RequestType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

class PeerServer(
    private val port: Int = 8990,
    private val scope: CoroutineScope,
    private val onRequestReceived: (LiveSnapRequest) -> Unit,
    private val onResponseReceived: (LiveSnapResponse) -> Unit,
    private val onScreenShareStopped: () -> Unit,
    private val onChatMessageReceived: (ChatMessage) -> Unit
) {
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val TAG = "PeerServer"

    @Volatile
    var isRunning: Boolean = false
        private set

    fun start() {
        if (isRunning) return
        serverJob = scope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(port)
                isRunning = true
                Log.i(TAG, "PeerServer started on port $port")

                while (isActive && !serverSocket!!.isClosed) {
                    try {
                        val clientSocket = serverSocket!!.accept()
                        launch(Dispatchers.IO) {
                            handleClient(clientSocket)
                        }
                    } catch (e: Exception) {
                        if (!isActive) break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start PeerServer on port $port", e)
            } finally {
                isRunning = false
            }
        }
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            // Ignored
        }
        serverJob?.cancel()
    }

    private fun handleClient(socket: Socket) {
        socket.use { s ->
            try {
                val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                val out = s.getOutputStream()

                val requestLine = reader.readLine() ?: return
                val parts = requestLine.split(" ")
                if (parts.size < 2) return
                val method = parts[0]
                val path = parts[1]

                var contentLength = 0
                var line: String? = reader.readLine()
                while (!line.isNullOrEmpty()) {
                    if (line.lowercase().startsWith("content-length:")) {
                        contentLength = line.substring(15).trim().toIntOrNull() ?: 0
                    }
                    line = reader.readLine()
                }

                val body = if (contentLength > 0) {
                    val buffer = CharArray(contentLength)
                    var read = 0
                    while (read < contentLength) {
                        val count = reader.read(buffer, read, contentLength - read)
                        if (count == -1) break
                        read += count
                    }
                    String(buffer, 0, read)
                } else {
                    ""
                }

                when {
                    method == "GET" && path == "/api/ping" -> {
                        sendHttpResponse(out, 200, """{"status":"ok","server":"LiveSnap"}""")
                    }
                    method == "POST" && path == "/api/request_snap" -> {
                        try {
                            val json = JSONObject(body)
                            val typeStr = json.optString("requestType", "PHOTO")
                            val reqType = if (typeStr == "SCREEN") RequestType.SCREEN else RequestType.PHOTO

                            val snapReq = LiveSnapRequest(
                                requestId = json.getString("requestId"),
                                requestType = reqType,
                                requesterId = json.optString("requesterId", "unknown"),
                                requesterUsername = json.getString("requesterUsername"),
                                requesterName = json.optString("requesterName", "Friend"),
                                requesterEmoji = json.optString("requesterEmoji", "⚡"),
                                requesterHost = json.optString("requesterHost", s.inetAddress.hostAddress ?: "127.0.0.1"),
                                requesterPort = json.optInt("requesterPort", 8990),
                                targetUsername = json.optString("targetUsername", ""),
                                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                                status = RequestStatus.PENDING
                            )
                            onRequestReceived(snapReq)
                            sendHttpResponse(out, 200, """{"status":"received"}""")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing snap request", e)
                            sendHttpResponse(out, 400, """{"error":"Invalid request payload"}""")
                        }
                    }
                    method == "POST" && path == "/api/respond_snap" -> {
                        try {
                            val json = JSONObject(body)
                            val typeStr = json.optString("requestType", "PHOTO")
                            val reqType = if (typeStr == "SCREEN") RequestType.SCREEN else RequestType.PHOTO

                            val snapResp = LiveSnapResponse(
                                requestId = json.getString("requestId"),
                                requestType = reqType,
                                responderUsername = json.getString("responderUsername"),
                                allowed = json.getBoolean("allowed"),
                                imageBase64 = json.optString("imageBase64", null),
                                declineReason = json.optString("declineReason", null),
                                timestamp = json.optLong("timestamp", System.currentTimeMillis())
                            )
                            onResponseReceived(snapResp)
                            sendHttpResponse(out, 200, """{"status":"response_acknowledged"}""")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing snap response", e)
                            sendHttpResponse(out, 400, """{"error":"Invalid response payload"}""")
                        }
                    }
                    method == "POST" && path == "/api/stop_screen" -> {
                        onScreenShareStopped()
                        sendHttpResponse(out, 200, """{"status":"screen_share_stopped"}""")
                    }
                    method == "POST" && path == "/api/chat" -> {
                        try {
                            val json = JSONObject(body)
                            val msg = ChatMessage(
                                id = json.getString("id"),
                                senderUsername = json.getString("senderUsername"),
                                senderDisplayName = json.optString("senderDisplayName", "Friend"),
                                senderEmoji = json.optString("senderEmoji", "👤"),
                                recipientUsername = json.getString("recipientUsername"),
                                text = json.getString("text"),
                                timestamp = json.optLong("timestamp", System.currentTimeMillis())
                            )
                            onChatMessageReceived(msg)
                            sendHttpResponse(out, 200, """{"status":"chat_received"}""")
                        } catch (e: Exception) {
                            sendHttpResponse(out, 400, """{"error":"Invalid chat payload"}""")
                        }
                    }
                    else -> {
                        sendHttpResponse(out, 404, """{"error":"Not found"}""")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Client handle error", e)
            }
        }
    }

    private fun sendHttpResponse(out: OutputStream, code: Int, jsonBody: String) {
        val statusText = if (code == 200) "OK" else if (code == 404) "Not Found" else "Bad Request"
        val bytes = jsonBody.toByteArray(Charsets.UTF_8)
        val header = "HTTP/1.1 $code $statusText\r\n" +
                "Content-Type: application/json; charset=UTF-8\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray(Charsets.UTF_8))
        out.write(bytes)
        out.flush()
    }
}
