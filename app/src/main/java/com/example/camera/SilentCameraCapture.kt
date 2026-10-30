package com.example.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

object SilentCameraCapture {

    /**
     * Captures a photo from the front camera completely in the background
     * without opening any camera screen, viewfinder, or camera app.
     */
    fun captureSilentFrontPhoto(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        fallbackUsername: String = "Me",
        fallbackEmoji: String = "📸",
        onSuccess: (Bitmap) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    val fallback = CameraHelper.createFallbackSelfie(fallbackUsername, fallbackEmoji)
                    onSuccess(fallback)
                    return@addListener
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageCapture
                )

                // Capture immediately in background
                imageCapture.takePicture(
                    executor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(imageProxy: ImageProxy) {
                            try {
                                val rotation = imageProxy.imageInfo.rotationDegrees.toFloat()
                                val bitmap = imageProxy.toBitmap()
                                val corrected = CameraHelper.rotateBitmap(bitmap, rotation, mirrorHorizontal = true)
                                imageProxy.close()
                                cameraProvider.unbindAll()
                                onSuccess(corrected)
                            } catch (e: Exception) {
                                imageProxy.close()
                                cameraProvider.unbindAll()
                                Log.e("SilentCameraCapture", "Error decoding proxy", e)
                                val fallback = CameraHelper.createFallbackSelfie(fallbackUsername, fallbackEmoji)
                                onSuccess(fallback)
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.w("SilentCameraCapture", "Capture error, using simulated live selfie", exception)
                            cameraProvider.unbindAll()
                            val fallback = CameraHelper.createFallbackSelfie(fallbackUsername, fallbackEmoji)
                            onSuccess(fallback)
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("SilentCameraCapture", "Silent camera setup failed", e)
                val fallback = CameraHelper.createFallbackSelfie(fallbackUsername, fallbackEmoji)
                onSuccess(fallback)
            }
        }, executor)
    }
}
