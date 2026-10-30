package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NRZ NOX", appName)
  }

  @Test
  fun `test fallback selfie generation`() {
    val bitmap = com.example.camera.CameraHelper.createFallbackSelfie("test_user", "📸")
    assertNotNull(bitmap)
    assertEquals(480, bitmap.width)
    assertEquals(640, bitmap.height)
    val base64 = com.example.camera.CameraHelper.bitmapToBase64(bitmap)
    assertTrue(base64.isNotEmpty())
    val restored = com.example.camera.CameraHelper.base64ToBitmap(base64)
    assertNotNull(restored)
  }
}
