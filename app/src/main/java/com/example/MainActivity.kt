package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.LiveSnapViewModel
import com.example.ui.screens.LiveSnapHomeScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.LiveSnapTheme

class MainActivity : ComponentActivity() {

    private val viewModel: LiveSnapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appTheme by viewModel.appTheme.collectAsState()
            var isSplashVisible by remember { mutableStateOf(true) }

            LiveSnapTheme(themeMode = appTheme) {
                if (isSplashVisible) {
                    SplashScreen(
                        onSplashFinished = { isSplashVisible = false }
                    )
                } else {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(400))
                    ) {
                        LiveSnapHomeScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshNetworkIp()
    }
}
