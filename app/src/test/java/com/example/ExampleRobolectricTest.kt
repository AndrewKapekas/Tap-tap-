package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.TapPreferences
import com.example.model.TapConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TapTap", appName)
  }

  @Test
  fun `verify tap preferences persist correctly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val preferences = TapPreferences(context)
    val config = TapConfig(tapIntervalMs = 40L, safeMode = true, targetCount = 2)
    preferences.saveConfig(config)

    val loaded = preferences.loadConfig()
    assertEquals(40L, loaded.tapIntervalMs)
    assertEquals(true, loaded.safeMode)
    assertEquals(2, loaded.targetCount)
  }
}
