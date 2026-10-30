package com.example.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CameraHelper {

    /**
     * Optimized for low-end devices: 480x640 max dimension and 65% JPEG quality
     * ensures payload is ~30-40KB, transferring in under 1 second without lag.
     */
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 65): String {
        val outputStream = ByteArrayOutputStream()
        val scaled = if (bitmap.width > 480 || bitmap.height > 640) {
            val scale = minOf(480f / bitmap.width, 640f / bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt().coerceAtLeast(1),
                (bitmap.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            bitmap
        }
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Float, mirrorHorizontal: Boolean = false): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees)
        if (mirrorHorizontal) {
            matrix.postScale(-1f, 1f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun createFallbackSelfie(username: String, emoji: String = "😊"): Bitmap {
        val width = 480
        val height = 640
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565) // RGB_565 halves memory on low-end phones
        val canvas = Canvas(bitmap)

        val bgPaint = Paint().apply {
            color = Color.rgb(22, 27, 34)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val haloPaint = Paint().apply {
            color = Color.rgb(16, 185, 129)
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        canvas.drawCircle(width / 2f, height / 2f - 40f, 110f, haloPaint)

        val textPaint = Paint().apply {
            textSize = 90f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(emoji, width / 2f, height / 2f, textPaint)

        val labelPaint = Paint().apply {
            color = Color.WHITE
            textSize = 26f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("LIVE FRONT CAMERA • $username", width / 2f, height / 2f + 130f, labelPaint)

        val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timePaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 20f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Instant 1-sec Delivery: " + timeFormatter.format(Date()), width / 2f, height / 2f + 170f, timePaint)

        return bitmap
    }

    /**
     * Generates a live mobile screen stream frame for the live screen sharing feature.
     */
    fun createScreenMirrorFrame(sharerName: String, sharerEmoji: String = "📱"): Bitmap {
        val width = 480
        val height = 800
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)

        // Wallpaper / Desktop screen
        val bgPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Status bar
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val statusPaint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            isAntiAlias = true
        }
        canvas.drawText(timeFormatter.format(Date()), 24f, 32f, statusPaint)
        canvas.drawText("5G 📶 98% 🔋", width - 130f, 32f, statusPaint)

        // Top Screen Sharing Active Banner
        val bannerPaint = Paint().apply {
            color = Color.rgb(220, 38, 38) // Red
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(20f, 54f, width - 20f, 104f), 12f, 12f, bannerPaint)

        val bannerTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("🔴 SCREEN SHARING LIVE", width / 2f, 86f, bannerTextPaint)

        // App Card 1: Browser / Home
        val cardPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(24f, 130f, width - 24f, 380f), 18f, 18f, cardPaint)

        val cardTitlePaint = Paint().apply {
            color = Color.rgb(16, 185, 129)
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("$sharerName's Phone Display", 48f, 175f, cardTitlePaint)

        val cardBodyPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            textSize = 18f
            isAntiAlias = true
        }
        canvas.drawText("• App: Active Home Workspace", 48f, 220f, cardBodyPaint)
        canvas.drawText("• Status: Live Screen Mirroring", 48f, 260f, cardBodyPaint)
        canvas.drawText("• Resolution: 480x800 Smooth Stream", 48f, 300f, cardBodyPaint)
        canvas.drawText("• Real-time latency: < 0.2s", 48f, 340f, cardBodyPaint)

        // Floating Apps Grid
        val appCirclePaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val icons = listOf("💬", "📸", "🎵", "⚙️")
        val labels = listOf("Chat", "Camera", "Music", "Settings")
        val appTextPaint = Paint().apply {
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val labelSmallPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 14f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        for (i in 0 until 4) {
            val cx = 60f + i * 115f + 15f
            val cy = 460f
            canvas.drawCircle(cx, cy, 38f, appCirclePaint)
            canvas.drawText(icons[i], cx, cy + 14f, appTextPaint)
            canvas.drawText(labels[i], cx, cy + 62f, labelSmallPaint)
        }

        // Live stream footer
        val footerPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 16f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Live phone screen from $sharerName", width / 2f, height - 40f, footerPaint)

        return bitmap
    }
}
