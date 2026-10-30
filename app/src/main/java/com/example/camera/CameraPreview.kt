package com.example.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onImageCaptureReady: (ImageCapture) -> Unit,
    onCameraUnavailable: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                onImageCaptureReady(imageCapture)

                // Select front camera first, fallback to back camera
                val cameraSelector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    onCameraUnavailable()
                    return@addListener
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraPreview", "Camera binding failed", e)
                onCameraUnavailable()
            }
        }, executor)

        onDispose {
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    )
}

fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    fallbackUsername: String,
    fallbackEmoji: String,
    onSuccess: (Bitmap) -> Unit,
    onError: (String) -> Unit
) {
    if (imageCapture == null) {
        // Fallback for emulator or uninitialized camera
        val fallback = CameraHelper.createFallbackSelfie(fallbackUsername, fallbackEmoji)
        onSuccess(fallback)
        return
    }

    val executor = ContextCompat.getMainExecutor(context)
    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                try {
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees.toFloat()
                    val bitmap = imageProxy.toBitmap()
                    val correctedBitmap = CameraHelper.rotateBitmap(bitmap, rotationDegrees, mirrorHorizontal = true)
                    imageProxy.close()
                    onSuccess(correctedBitmap)
                } catch (e: Exception) {
                    imageProxy.close()
                    Log.e("CameraCapture", "Failed converting image", e)
                    val fallback = CameraHelper.createFallbackSelfie(fallbackUsername, fallbackEmoji)
                    onSuccess(fallback)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.w("CameraCapture", "Camera capture error, falling back to live simulation", exception)
                val fallback = CameraHelper.createFallbackSelfie(fallbackUsername, fallbackEmoji)
                onSuccess(fallback)
            }
        }
    )
}
